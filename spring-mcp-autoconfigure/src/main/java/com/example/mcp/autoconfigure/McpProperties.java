package com.example.mcp.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the MCP server.
 * All properties are prefixed with {@code mcp}.
 */
@ConfigurationProperties(prefix = "mcp")
public class McpProperties {

    /** Whether the MCP server is enabled. */
    private boolean enabled = true;

    /** Logical name of this MCP server instance. */
    private String serverName = "spring-mcp-server";

    /** Server version advertised to clients. */
    private String serverVersion = "1.0.0";

    public boolean isEnabled()                  { return enabled; }
    public void setEnabled(boolean enabled)     { this.enabled = enabled; }

    public String getServerName()               { return serverName; }
    public void setServerName(String name)      { this.serverName = name; }

    public String getServerVersion()            { return serverVersion; }
    public void setServerVersion(String ver)    { this.serverVersion = ver; }
}