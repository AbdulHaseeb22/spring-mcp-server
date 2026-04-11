package io.github.abdulhaseeb22.mcp.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.abdulhaseeb22.mcp.core.McpToolDefinition;
import io.github.abdulhaseeb22.mcp.core.McpToolDefinition.McpParamDefinition;
import io.github.abdulhaseeb22.mcp.core.McpToolRegistry;
import io.github.abdulhaseeb22.mcp.core.jsonrpc.JsonRpcError;
import io.github.abdulhaseeb22.mcp.core.jsonrpc.JsonRpcRequest;
import io.github.abdulhaseeb22.mcp.core.jsonrpc.JsonRpcResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link McpRequestHandler}.
 *
 * <p>{@link McpToolRegistry} is mocked so each test exercises only the routing
 * and response-shaping logic — not registry or reflection behaviour.
 */
@ExtendWith(MockitoExtension.class)
class McpRequestHandlerTest {

    // -------------------------------------------------------------------------
    // Helper bean for reflection-based invocation tests
    // -------------------------------------------------------------------------

    static class EchoService {
        public String echo(String input) {
            return "Echo: " + input;
        }
    }

    // -------------------------------------------------------------------------
    // SUT
    // -------------------------------------------------------------------------

    @Mock
    McpToolRegistry registry;

    McpRequestHandler handler;

    @BeforeEach
    void setUp() {
        handler = new McpRequestHandler(registry, new ObjectMapper());
    }

    // -------------------------------------------------------------------------
    // tools/list
    // -------------------------------------------------------------------------

    @Test
    void toolsList_responseContainsToolsKey() throws NoSuchMethodException {
        when(registry.getAllTools()).thenReturn(List.of(echoTool()));

        JsonRpcResponse response = handler.handle(toolsListRequest());

        assertThat(response.error()).isNull();
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.result();
        assertThat(result.get("tools")).isNotNull();
        assertThat((List<?>) result.get("tools")).hasSize(1);
    }

    // -------------------------------------------------------------------------
    // tools/call — success
    // -------------------------------------------------------------------------

    @Test
    void toolsCall_validTool_returnsContentArray() throws NoSuchMethodException {
        when(registry.findTool("echo")).thenReturn(Optional.of(echoTool()));

        JsonRpcResponse response = handler.handle(toolsCallRequest("echo", Map.of("input", "hello")));

        assertThat(response.error()).isNull();
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.result();
        assertThat(result.get("content")).isNotNull();

        @SuppressWarnings("unchecked")
        List<Map<String, String>> content = (List<Map<String, String>>) result.get("content");
        assertThat(content).hasSize(1);
        Map<String, String> item = content.get(0);
        assertThat(item.get("type")).isEqualTo("text");
        assertThat(item.get("text").toString()).contains("Echo: hello");
    }

    // -------------------------------------------------------------------------
    // tools/call — errors
    // -------------------------------------------------------------------------

    @Test
    void toolsCall_unknownToolName_returnsMethodNotFoundError() {
        when(registry.findTool("ghost")).thenReturn(Optional.empty());

        JsonRpcResponse response = handler.handle(toolsCallRequest("ghost", Map.of()));

        assertThat(response.result()).isNull();
        assertThat(response.error()).isNotNull();
        assertThat(response.error().code()).isEqualTo(JsonRpcError.METHOD_NOT_FOUND);
    }

    @Test
    void toolsCall_missingNameParam_returnsInvalidParamsError() {
        // params map present but "name" key absent
        JsonRpcRequest request = new JsonRpcRequest("2.0", "tools/call", 1, Map.of());

        JsonRpcResponse response = handler.handle(request);

        assertThat(response.result()).isNull();
        assertThat(response.error()).isNotNull();
        assertThat(response.error().code()).isEqualTo(JsonRpcError.INVALID_PARAMS);
    }

    // -------------------------------------------------------------------------
    // Unknown method
    // -------------------------------------------------------------------------

    @Test
    void unknownMethod_returnsMethodNotFoundError() {
        JsonRpcRequest request = new JsonRpcRequest("2.0", "unknown/method", 1, null);

        JsonRpcResponse response = handler.handle(request);

        assertThat(response.result()).isNull();
        assertThat(response.error()).isNotNull();
        assertThat(response.error().code()).isEqualTo(JsonRpcError.METHOD_NOT_FOUND);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private McpToolDefinition echoTool() throws NoSuchMethodException {
        Method method = EchoService.class.getMethod("echo", String.class);
        McpParamDefinition param = new McpParamDefinition("input", "Text to echo", true, String.class);
        return new McpToolDefinition("echo", "Echoes the input", new EchoService(), method, List.of(param));
    }

    private JsonRpcRequest toolsListRequest() {
        return new JsonRpcRequest("2.0", "tools/list", 1, null);
    }

    private JsonRpcRequest toolsCallRequest(String toolName, Map<String, Object> arguments) {
        return new JsonRpcRequest("2.0", "tools/call", 1,
                Map.of("name", toolName, "arguments", arguments));
    }
}
