package io.github.abdulhaseeb22.mcp.autoconfigure;

import io.github.abdulhaseeb22.mcp.annotations.McpParam;
import io.github.abdulhaseeb22.mcp.annotations.McpServer;
import io.github.abdulhaseeb22.mcp.annotations.McpTool;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test that boots a real (mock-servlet) Spring context and
 * exercises the full pipeline: tool scanning → request handling → HTTP layer.
 *
 * <p>{@link TestConfig} uses {@code @SpringBootConfiguration + @EnableAutoConfiguration}
 * without {@code @ComponentScan} so only explicitly declared beans are registered —
 * this prevents the scanner from accidentally picking up the autoconfigure source
 * classes that live in the same package.
 */
@SpringBootTest(classes = McpControllerIntegrationTest.TestConfig.class,
                webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
class McpControllerIntegrationTest {

    // -------------------------------------------------------------------------
    // Test application context
    // -------------------------------------------------------------------------

    /**
     * Minimal Boot configuration: {@code @EnableAutoConfiguration} loads
     * {@link McpAutoConfiguration} (and Spring MVC) from the auto-configuration
     * registry. No component scan — beans are registered explicitly below.
     */
    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestConfig {

        /** Explicitly expose the test MCP server so McpToolScanner processes it. */
        @Bean
        TestTools testTools() {
            return new TestTools();
        }
    }

    // -------------------------------------------------------------------------
    // Test MCP server
    // -------------------------------------------------------------------------

    @McpServer(name = "test", description = "Integration test MCP server")
    static class TestTools {

        @McpTool(name = "ping", description = "Responds to a ping with pong")
        public String ping(
                @McpParam(name = "message", description = "The message to echo back") String message) {
            return "pong: " + message;
        }
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Autowired
    MockMvc mockMvc;

    @Test
    void health_returnsStatusUp() throws Exception {
        mockMvc.perform(get("/mcp/health"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("UP"))
               .andExpect(jsonPath("$.protocol").value("MCP/JSON-RPC 2.0"));
    }

    @Test
    void toolsList_returnsPingToolWithInputSchema() throws Exception {
        mockMvc.perform(post("/mcp")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("{\"jsonrpc\":\"2.0\",\"method\":\"tools/list\",\"id\":1}"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.jsonrpc").value("2.0"))
               .andExpect(jsonPath("$.id").value(1))
               .andExpect(jsonPath("$.result.tools").isArray())
               .andExpect(jsonPath("$.result.tools[0].name").value("ping"))
               .andExpect(jsonPath("$.result.tools[0].description").exists())
               .andExpect(jsonPath("$.result.tools[0].inputSchema.type").value("object"))
               .andExpect(jsonPath("$.result.tools[0].inputSchema.properties.message").exists());
    }
}
