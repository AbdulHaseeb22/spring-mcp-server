package com.example.mcp.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes a parameter of an {@link McpTool}-annotated method.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpParam {

    /** Parameter name exposed to the model. */
    String name();

    /** Human-readable description of this parameter. */
    String description() default "";

    /** Whether this parameter is required. */
    boolean required() default true;
}