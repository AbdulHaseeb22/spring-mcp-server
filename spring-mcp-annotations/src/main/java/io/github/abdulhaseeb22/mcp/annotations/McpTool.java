package io.github.abdulhaseeb22.mcp.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an MCP (Model Context Protocol) tool that can be invoked
 * by an AI model client.
 *
 * <p>This annotation must be placed on a method within a class annotated with
 * {@link McpServer}. The method and its parameters are introspected at startup
 * to build a tool descriptor that is advertised to MCP clients via
 * {@code tools/list} and dispatched on {@code tools/call}.
 *
 * <p>If {@link #name()} is left empty the method name is used as the tool name.
 * Tool names must be unique within a single {@link McpServer} component.
 *
 * <p>Example usage:
 * <pre>{@code
 * @McpTool(name = "get_current_weather",
 *          description = "Returns the current weather conditions for the given city.")
 * public String getCurrentWeather(@McpParam(name = "city") String city) { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpTool {

    /**
     * The name of the tool as advertised to MCP clients.
     * Convention is {@code snake_case} (e.g. {@code get_current_weather}).
     * If empty, the declaring method's name is used instead.
     */
    String name() default "";

    /**
     * A human-readable description of what this tool does.
     * This is surfaced to the AI model to help it decide when and how to invoke
     * the tool, so it should be clear and concise.
     * This attribute is required — every tool must have a description.
     */
    String description();
}