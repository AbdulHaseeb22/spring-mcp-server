package io.github.abdulhaseeb22.mcp.core;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Immutable descriptor for a single MCP tool discovered at application startup.
 *
 * <p>Each instance represents one {@code @McpTool}-annotated method together with
 * its owning bean, reflective handle, and the metadata for every parameter.
 *
 * @param name        Tool name advertised to MCP clients (snake_case by convention).
 * @param description Human-readable description surfaced to the AI model.
 * @param targetBean  The Spring bean instance on which the method is invoked.
 * @param method      Reflective handle to the tool method.
 * @param params      Ordered list of parameter descriptors matching the method signature.
 */
public record McpToolDefinition(
        String name,
        String description,
        Object targetBean,
        Method method,
        List<McpParamDefinition> params
) {

    /** Defensive copy — callers cannot mutate the params list after construction. */
    public McpToolDefinition {
        params = List.copyOf(params);
    }

    /**
     * Immutable descriptor for a single parameter of an MCP tool method.
     *
     * @param name        Parameter name exposed in the tool's JSON Schema.
     * @param description Human-readable hint for the AI model.
     * @param required    Whether the caller must supply this parameter.
     * @param type        Compiled Java type of the parameter.
     */
    public record McpParamDefinition(
            String name,
            String description,
            boolean required,
            Class<?> type
    ) {}
}