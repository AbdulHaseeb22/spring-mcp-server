package io.github.abdulhaseeb22.mcp.autoconfigure;

import io.github.abdulhaseeb22.mcp.annotations.McpParam;
import io.github.abdulhaseeb22.mcp.annotations.McpServer;
import io.github.abdulhaseeb22.mcp.annotations.McpTool;
import io.github.abdulhaseeb22.mcp.core.McpToolDefinition;
import io.github.abdulhaseeb22.mcp.core.McpToolDefinition.McpParamDefinition;
import io.github.abdulhaseeb22.mcp.core.McpToolRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * Spring {@link BeanPostProcessor} that discovers MCP tools at application startup.
 *
 * <p>After each bean is fully initialized, this scanner checks whether the bean's
 * class carries {@link McpServer}. If it does, every public method annotated with
 * {@link McpTool} is introspected, wrapped in a {@link McpToolDefinition}, and
 * registered in the shared {@link McpToolRegistry}.
 *
 * <p>Name-resolution rules:
 * <ul>
 *   <li>Tool name — {@link McpTool#name()} if non-empty, otherwise the method name.</li>
 *   <li>Param name — {@link McpParam#name()} if non-empty, otherwise the compiled
 *       parameter name (requires {@code -parameters} javac flag).</li>
 * </ul>
 */
public class McpToolScanner implements BeanPostProcessor {

    private final McpToolRegistry registry;

    public McpToolScanner(McpToolRegistry registry) {
        this.registry = registry;
    }

    /**
     * Scans the bean for {@link McpTool}-annotated methods after it is initialized.
     * Non-{@link McpServer} beans are returned unchanged with no overhead beyond a
     * single annotation lookup.
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();

        // AnnotationUtils.findAnnotation walks the class hierarchy and handles
        // CGLIB-proxied classes transparently.
        McpServer serverAnnotation = AnnotationUtils.findAnnotation(targetClass, McpServer.class);
        if (serverAnnotation == null) {
            return bean;
        }

        for (Method method : targetClass.getMethods()) {
            McpTool toolAnnotation = AnnotationUtils.findAnnotation(method, McpTool.class);
            if (toolAnnotation == null) {
                continue;
            }

            String toolName = toolAnnotation.name().isEmpty()
                    ? method.getName()
                    : toolAnnotation.name();

            List<McpParamDefinition> paramDefs = buildParamDefs(method);

            registry.register(new McpToolDefinition(
                    toolName,
                    toolAnnotation.description(),
                    bean,
                    method,
                    paramDefs));
        }

        return bean;
    }

    /**
     * Builds an ordered list of {@link McpParamDefinition}s matching the method's
     * parameter list. Parameters without {@link McpParam} are still included using
     * their compiled name and {@code required = true}.
     */
    private List<McpParamDefinition> buildParamDefs(Method method) {
        List<McpParamDefinition> defs = new ArrayList<>();

        for (Parameter parameter : method.getParameters()) {
            McpParam paramAnnotation = parameter.getAnnotation(McpParam.class);

            String name = (paramAnnotation != null && !paramAnnotation.name().isEmpty())
                    ? paramAnnotation.name()
                    : parameter.getName();           // requires -parameters javac flag

            String description = paramAnnotation != null ? paramAnnotation.description() : "";
            boolean required   = paramAnnotation == null || paramAnnotation.required();

            defs.add(new McpParamDefinition(name, description, required, parameter.getType()));
        }

        return defs;
    }
}