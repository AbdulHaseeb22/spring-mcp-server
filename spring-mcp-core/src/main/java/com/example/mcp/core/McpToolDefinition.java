package com.example.mcp.core;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Immutable descriptor for a discovered MCP tool method.
 */
public final class McpToolDefinition {

    private final String name;
    private final String description;
    private final Object bean;
    private final Method method;
    private final List<McpParamDefinition> params;

    public McpToolDefinition(String name, String description, Object bean,
                             Method method, List<McpParamDefinition> params) {
        this.name = name;
        this.description = description;
        this.bean = bean;
        this.method = method;
        this.params = List.copyOf(params);
    }

    public String getName()                      { return name; }
    public String getDescription()               { return description; }
    public Object getBean()                      { return bean; }
    public Method getMethod()                    { return method; }
    public List<McpParamDefinition> getParams()  { return params; }

    @Override
    public String toString() {
        return "McpToolDefinition{name='" + name + "', params=" + params.size() + "}";
    }
}