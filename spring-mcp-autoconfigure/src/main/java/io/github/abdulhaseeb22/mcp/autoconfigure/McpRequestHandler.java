package io.github.abdulhaseeb22.mcp.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.abdulhaseeb22.mcp.core.McpToolDefinition;
import io.github.abdulhaseeb22.mcp.core.McpToolDefinition.McpParamDefinition;
import io.github.abdulhaseeb22.mcp.core.McpToolRegistry;
import io.github.abdulhaseeb22.mcp.core.jsonrpc.JsonRpcError;
import io.github.abdulhaseeb22.mcp.core.jsonrpc.JsonRpcRequest;
import io.github.abdulhaseeb22.mcp.core.jsonrpc.JsonRpcResponse;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Routes incoming JSON-RPC 2.0 requests to the appropriate MCP handler.
 *
 * <p>Supported methods:
 * <ul>
 *   <li>{@code tools/list} — returns all registered tools with their JSON Schema.</li>
 *   <li>{@code tools/call} — looks up a tool by name, coerces arguments, invokes
 *       it via reflection, and wraps the result in MCP content format.</li>
 * </ul>
 *
 * <p>All unknown methods receive a {@code -32601 Method not found} error response.
 * Reflection failures are wrapped in a {@code -32603 Internal error} response so
 * the transport layer never has to deal with raw exceptions.
 */
public class McpRequestHandler {

    private final McpToolRegistry registry;
    private final ObjectMapper    objectMapper;

    public McpRequestHandler(McpToolRegistry registry, ObjectMapper objectMapper) {
        this.registry     = registry;
        this.objectMapper = objectMapper;
    }

    /**
     * Dispatches the request to the correct handler and returns a well-formed
     * JSON-RPC response in all cases — success or error.
     *
     * @param request the decoded JSON-RPC request
     * @return a JSON-RPC response ready for serialization
     */
    public JsonRpcResponse handle(JsonRpcRequest request) {
        return switch (request.method()) {
            case "tools/list" -> handleToolsList(request);
            case "tools/call" -> handleToolsCall(request);
            default           -> JsonRpcResponse.error(request.id(),
                                     JsonRpcError.methodNotFound(request.method()));
        };
    }

    // -------------------------------------------------------------------------
    // tools/list
    // -------------------------------------------------------------------------

    /**
     * Returns a map {@code {"tools": [...]}} where each entry contains the tool's
     * name, description, and a JSON Schema describing its input parameters.
     */
    private JsonRpcResponse handleToolsList(JsonRpcRequest request) {
        List<Map<String, Object>> toolList = new ArrayList<>();

        for (McpToolDefinition tool : registry.getAllTools()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name",        tool.name());
            entry.put("description", tool.description());
            entry.put("inputSchema", buildInputSchema(tool));
            toolList.add(entry);
        }

        return JsonRpcResponse.success(request.id(), Map.of("tools", toolList));
    }

    /**
     * Builds a JSON Schema {@code object} descriptor for the tool's input parameters.
     * Each parameter becomes a property; required parameters are listed in
     * the schema's {@code required} array.
     */
    private Map<String, Object> buildInputSchema(McpToolDefinition tool) {
        Map<String, Object> schema     = new LinkedHashMap<>();
        Map<String, Object> properties = new LinkedHashMap<>();
        List<String>        required   = new ArrayList<>();

        for (McpParamDefinition param : tool.params()) {
            Map<String, Object> propSchema = new LinkedHashMap<>();
            propSchema.put("type", toJsonSchemaType(param.type()));
            if (!param.description().isEmpty()) {
                propSchema.put("description", param.description());
            }
            properties.put(param.name(), propSchema);
            if (param.required()) {
                required.add(param.name());
            }
        }

        schema.put("type",       "object");
        schema.put("properties", properties);
        if (!required.isEmpty()) {
            schema.put("required", required);
        }
        return schema;
    }

    /** Maps a Java type to its closest JSON Schema primitive type string. */
    private String toJsonSchemaType(Class<?> type) {
        if (type == String.class)                                               return "string";
        if (type == boolean.class || type == Boolean.class)                    return "boolean";
        if (type == int.class     || type == Integer.class
         || type == long.class    || type == Long.class
         || type == short.class   || type == Short.class)                      return "integer";
        if (type == double.class  || type == Double.class
         || type == float.class   || type == Float.class)                      return "number";
        return "string"; // safe fallback — toString() is always available
    }

    // -------------------------------------------------------------------------
    // tools/call
    // -------------------------------------------------------------------------

    /**
     * Resolves and invokes a tool by name.
     *
     * <p>Expected {@code params} structure:
     * <pre>{@code
     * {
     *   "name":      "tool_name",
     *   "arguments": { "param1": "value1", ... }
     * }
     * }</pre>
     */
    private JsonRpcResponse handleToolsCall(JsonRpcRequest request) {
        if (request.params() == null) {
            return JsonRpcResponse.error(request.id(),
                    JsonRpcError.invalidParams("params must not be null"));
        }

        Object toolNameRaw = request.param("name");
        if (toolNameRaw == null) {
            return JsonRpcResponse.error(request.id(),
                    JsonRpcError.invalidParams("missing required field: name"));
        }
        String toolName = toolNameRaw.toString();

        return registry.findTool(toolName)
                .map(tool -> invokeTool(request, tool))
                .orElse(JsonRpcResponse.error(request.id(),
                        JsonRpcError.methodNotFound(toolName)));
    }

    @SuppressWarnings("unchecked")
    private JsonRpcResponse invokeTool(JsonRpcRequest request, McpToolDefinition tool) {
        // "arguments" is the MCP-spec key for tool call arguments
        Map<String, Object> arguments = Collections.emptyMap();
        Object argsRaw = request.param("arguments");
        if (argsRaw instanceof Map<?, ?> map) {
            arguments = (Map<String, Object>) map;
        }

        Object[] args;
        try {
            args = resolveArguments(tool, arguments);
        } catch (IllegalArgumentException e) {
            return JsonRpcResponse.error(request.id(),
                    JsonRpcError.invalidParams(e.getMessage()));
        }

        try {
            Object result  = tool.method().invoke(tool.targetBean(), args);
            String text;
            try {
                text = (result != null) ? objectMapper.writeValueAsString(result) : "";
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                text = (result != null) ? result.toString() : "";
            }
            // MCP spec wraps tool output in a content array
            List<Map<String, String>> content = List.of(Map.of("type", "text", "text", text));
            return JsonRpcResponse.success(request.id(), Map.of("content", content));

        } catch (InvocationTargetException e) {
            String message = Optional.ofNullable(e.getCause())
                    .map(Throwable::getMessage)
                    .orElseGet(e::getMessage);
            return JsonRpcResponse.error(request.id(),
                    JsonRpcError.internalError(message));
        } catch (IllegalAccessException e) {
            return JsonRpcResponse.error(request.id(),
                    JsonRpcError.internalError("Method not accessible: " + tool.name()));
        }
    }

    /**
     * Converts the raw argument map into a typed array matching the method signature.
     * Type coercion is delegated to Jackson's {@link ObjectMapper#convertValue} so
     * that JSON numbers, strings, and booleans are all handled correctly.
     *
     * @throws IllegalArgumentException if a required argument is absent
     */
    private Object[] resolveArguments(McpToolDefinition tool, Map<String, Object> arguments) {
        List<McpParamDefinition> params = tool.params();
        Object[] args = new Object[params.size()];

        for (int i = 0; i < params.size(); i++) {
            McpParamDefinition param    = params.get(i);
            Object             rawValue = arguments.get(param.name());

            if (rawValue == null && param.required()) {
                throw new IllegalArgumentException("missing required argument: " + param.name());
            }
            args[i] = coerce(rawValue, param.type());
        }
        return args;
    }

    /** Coerces a raw JSON value to the target Java type via Jackson. */
    private Object coerce(Object value, Class<?> targetType) {
        if (value == null) return null;
        return objectMapper.convertValue(value, targetType);
    }
}