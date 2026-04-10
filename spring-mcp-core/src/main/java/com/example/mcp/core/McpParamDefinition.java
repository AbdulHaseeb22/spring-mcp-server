package com.example.mcp.core;

/**
 * Immutable descriptor for a single parameter of an MCP tool.
 */
public final class McpParamDefinition {

    private final String name;
    private final String description;
    private final boolean required;
    private final Class<?> type;

    public McpParamDefinition(String name, String description, boolean required, Class<?> type) {
        this.name = name;
        this.description = description;
        this.required = required;
        this.type = type;
    }

    public String getName()        { return name; }
    public String getDescription() { return description; }
    public boolean isRequired()    { return required; }
    public Class<?> getType()      { return type; }

    @Override
    public String toString() {
        return "McpParamDefinition{name='" + name + "', type=" + type.getSimpleName() + "}";
    }
}