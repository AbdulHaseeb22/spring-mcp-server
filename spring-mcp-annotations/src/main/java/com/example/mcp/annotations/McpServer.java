package com.example.mcp.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as an MCP server component whose {@link McpTool}-annotated
 * methods will be registered and exposed to AI models.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpServer {

    /** Optional logical name for this server component. */
    String value() default "";
}