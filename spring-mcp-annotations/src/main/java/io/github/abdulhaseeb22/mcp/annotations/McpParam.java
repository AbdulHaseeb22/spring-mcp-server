package io.github.abdulhaseeb22.mcp.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes a parameter of an {@link McpTool}-annotated method.
 *
 * <p>Metadata from this annotation is used to build the JSON Schema for the
 * tool's input object that is advertised to MCP clients. Parameters without
 * this annotation are still included in the schema using the compiled parameter
 * name and default settings.
 *
 * <p>Example usage:
 * <pre>{@code
 * @McpTool(description = "Returns an N-day weather forecast.")
 * public String getForecast(
 *         @McpParam(name = "city",  description = "City name, e.g. 'London'") String city,
 *         @McpParam(name = "days",  description = "Number of forecast days (1–7)",
 *                   required = false) int days) { ... }
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpParam {

    /**
     * The parameter name exposed in the tool's JSON Schema and used as the key
     * when the MCP client passes arguments. If empty, the compiled parameter
     * name is used (requires {@code -parameters} compiler flag to be meaningful).
     */
    String name() default "";

    /**
     * Human-readable description of what this parameter represents.
     * Included in the JSON Schema {@code description} field to help the AI
     * model supply correct values.
     */
    String description() default "";

    /**
     * Whether this parameter must be provided by the caller.
     * Required parameters are listed in the JSON Schema {@code required} array;
     * optional parameters are omitted from it.
     * Defaults to {@code true}.
     */
    boolean required() default true;
}