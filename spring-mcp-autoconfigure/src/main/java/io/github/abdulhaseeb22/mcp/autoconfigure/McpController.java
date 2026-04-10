package io.github.abdulhaseeb22.mcp.autoconfigure;

import io.github.abdulhaseeb22.mcp.core.jsonrpc.JsonRpcRequest;
import io.github.abdulhaseeb22.mcp.core.jsonrpc.JsonRpcResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * HTTP entry point for the MCP JSON-RPC transport.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code POST /mcp} — accepts a JSON-RPC 2.0 request body and returns a
 *       JSON-RPC 2.0 response. All MCP method routing is delegated to
 *       {@link McpRequestHandler}.</li>
 *   <li>{@code GET /mcp/health} — lightweight liveness probe; always returns
 *       {@code {"status":"UP"}} without touching the registry.</li>
 * </ul>
 *
 * <p>This controller is only registered when Spring MVC is on the classpath
 * (guarded by {@code @ConditionalOnWebApplication} in {@link McpAutoConfiguration}).
 */
@RestController
@RequestMapping("/mcp")
public class McpController {

    private final McpRequestHandler handler;

    public McpController(McpRequestHandler handler) {
        this.handler = handler;
    }

    /**
     * Handles a JSON-RPC 2.0 request and returns the corresponding response.
     *
     * <p>The response HTTP status is always {@code 200 OK} — per the JSON-RPC spec,
     * protocol-level errors (method not found, invalid params, etc.) are expressed
     * inside the response body, not via HTTP status codes.
     *
     * @param request the decoded JSON-RPC request
     * @return a JSON-RPC response (success or error)
     */
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonRpcResponse handle(@RequestBody JsonRpcRequest request) {
        return handler.handle(request);
    }

    /**
     * Lightweight health check for liveness/readiness probes.
     *
     * @return {@code {"status":"UP","protocol":"MCP/JSON-RPC 2.0"}}
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "protocol", "MCP/JSON-RPC 2.0");
    }
}