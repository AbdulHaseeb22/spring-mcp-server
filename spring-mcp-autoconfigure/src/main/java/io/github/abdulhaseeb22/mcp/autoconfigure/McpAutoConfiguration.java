package io.github.abdulhaseeb22.mcp.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.abdulhaseeb22.mcp.core.McpToolRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;

/**
 * Spring Boot auto-configuration for the MCP server.
 *
 * <p>Beans registered here:
 * <ol>
 *   <li>{@link McpToolRegistry} — always; holds the in-memory tool map.</li>
 *   <li>{@link McpToolScanner} — always; a {@code BeanPostProcessor} that
 *       discovers {@code @McpServer} beans at startup.</li>
 *   <li>{@link McpRequestHandler} — servlet-web apps only; routes JSON-RPC
 *       requests to tool implementations.</li>
 *   <li>{@link McpController} — servlet-web apps only; exposes the HTTP
 *       endpoints {@code POST /mcp} and {@code GET /mcp/health}.</li>
 * </ol>
 *
 * <p>Every bean is guarded by {@link ConditionalOnMissingBean} so applications
 * can supply their own implementation and opt out of the defaults.
 */
@AutoConfiguration
public class McpAutoConfiguration {

    /**
     * Central in-memory store for all discovered {@link McpToolRegistry} entries.
     */
    @Bean
    @ConditionalOnMissingBean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    McpToolRegistry mcpToolRegistry() {
        return new McpToolRegistry();
    }

    /**
     * {@code BeanPostProcessor} that scans {@code @McpServer} beans and populates
     * the registry. Declared as a {@code BeanPostProcessor} so Spring wires it
     * early in the lifecycle, before any application beans are used.
     */
    @Bean
    @ConditionalOnMissingBean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static McpToolScanner mcpToolScanner(McpToolRegistry registry) {
        return new McpToolScanner(registry);
    }

    /**
     * JSON-RPC request router. Requires an {@link ObjectMapper} bean, which
     * Spring Boot's Jackson auto-configuration always provides.
     * Only created in servlet-web applications.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWebApplication(type = Type.SERVLET)
    McpRequestHandler mcpRequestHandler(McpToolRegistry registry,
                                        ObjectMapper objectMapper) {
        return new McpRequestHandler(registry, objectMapper);
    }

    /**
     * REST controller that exposes {@code POST /mcp} and {@code GET /mcp/health}.
     * Only created in servlet-web applications.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWebApplication(type = Type.SERVLET)
    McpController mcpController(McpRequestHandler handler) {
        return new McpController(handler);
    }
}