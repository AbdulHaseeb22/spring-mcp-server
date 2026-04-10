package com.example.mcp.core;

import com.example.mcp.annotations.McpParam;
import com.example.mcp.annotations.McpServer;
import com.example.mcp.annotations.McpTool;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Scans the Spring application context for {@link McpServer}-annotated beans
 * and registers their {@link McpTool}-annotated methods.
 */
public class McpToolRegistry implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;
    private final Map<String, McpToolDefinition> tools = new LinkedHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        Map<String, Object> servers = applicationContext.getBeansWithAnnotation(McpServer.class);
        for (Object bean : servers.values()) {
            for (Method method : bean.getClass().getMethods()) {
                McpTool toolAnnotation = method.getAnnotation(McpTool.class);
                if (toolAnnotation == null) {
                    continue;
                }
                String toolName = toolAnnotation.name().isEmpty() ? method.getName() : toolAnnotation.name();
                List<McpParamDefinition> paramDefs = buildParamDefs(method);
                McpToolDefinition def = new McpToolDefinition(
                        toolName, toolAnnotation.description(), bean, method, paramDefs);
                tools.put(toolName, def);
            }
        }
    }

    private List<McpParamDefinition> buildParamDefs(Method method) {
        List<McpParamDefinition> defs = new ArrayList<>();
        for (Parameter param : method.getParameters()) {
            McpParam paramAnnotation = param.getAnnotation(McpParam.class);
            String name = paramAnnotation != null ? paramAnnotation.name() : param.getName();
            String desc = paramAnnotation != null ? paramAnnotation.description() : "";
            boolean required = paramAnnotation == null || paramAnnotation.required();
            defs.add(new McpParamDefinition(name, desc, required, param.getType()));
        }
        return defs;
    }

    public Map<String, McpToolDefinition> getTools() {
        return Collections.unmodifiableMap(tools);
    }

    public McpToolDefinition getTool(String name) {
        return tools.get(name);
    }
}