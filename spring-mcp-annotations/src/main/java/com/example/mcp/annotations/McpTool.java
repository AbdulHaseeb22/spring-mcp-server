package com.example.mcp.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an MCP tool that can be invoked by an AI model.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpTool {

    /** Display name of the tool. Defaults to the method name if empty. */
    String name() default "";

    /** Human-readable description of what this tool does. */
    String description() default "";
}