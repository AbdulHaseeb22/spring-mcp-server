package io.github.abdulhaseeb22.mcp.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Central in-memory registry for all discovered {@link McpToolDefinition}s.
 *
 * <p>Tools are registered at application startup by the scanner that processes
 * {@code @McpServer}-annotated beans. At runtime, the MCP transport layer
 * queries this registry to serve {@code tools/list} and dispatch
 * {@code tools/call} requests.
 *
 * <p>This class is intentionally framework-agnostic — it carries no Spring
 * annotations so it can be unit-tested without a Spring context.
 */
public class McpToolRegistry {

    private final Map<String, McpToolDefinition> tools = new LinkedHashMap<>();

    /**
     * Registers a tool definition.
     *
     * <p>If a tool with the same name was already registered it is replaced
     * and the previous entry is discarded.
     *
     * @param tool the tool descriptor to register; must not be {@code null}
     */
    public void register(McpToolDefinition tool) {
        if (tool == null) {
            throw new IllegalArgumentException("tool must not be null");
        }
        tools.put(tool.name(), tool);
    }

    /**
     * Looks up a tool by its advertised name.
     *
     * @param name the tool name as sent by an MCP client
     * @return an {@link Optional} containing the definition, or empty if not found
     */
    public Optional<McpToolDefinition> findTool(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    /**
     * Returns an unmodifiable snapshot of all registered tools in registration order.
     *
     * @return immutable list of all {@link McpToolDefinition}s
     */
    public List<McpToolDefinition> getAllTools() {
        return Collections.unmodifiableList(new ArrayList<>(tools.values()));
    }
}