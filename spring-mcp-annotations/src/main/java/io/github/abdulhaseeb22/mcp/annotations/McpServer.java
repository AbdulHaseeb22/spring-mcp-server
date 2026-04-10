package io.github.abdulhaseeb22.mcp.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Spring bean as an MCP (Model Context Protocol) server component.
 *
 * <p>Classes annotated with {@code @McpServer} are detected at application startup
 * and their {@link McpTool}-annotated methods are registered in the
 * {@code McpToolRegistry} for exposure to AI model clients.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Component
 * @McpServer(name = "weather", description = "Provides weather data tools")
 * public class WeatherTools {
 *     @McpTool(description = "Returns the current weather for a city")
 *     public String getCurrentWeather(@McpParam(name = "city") String city) { ... }
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpServer {

    /**
     * Logical name for this MCP server component.
     * Used to identify the server in registries and logs.
     * Defaults to an empty string, in which case the bean name is used.
     */
    String name() default "";

    /**
     * Human-readable description of what this server component provides.
     * Helps clients and developers understand the purpose of the server.
     */
    String description() default "";
}