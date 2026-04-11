package io.github.abdulhaseeb22.mcp.autoconfigure;

import io.github.abdulhaseeb22.mcp.annotations.McpParam;
import io.github.abdulhaseeb22.mcp.annotations.McpServer;
import io.github.abdulhaseeb22.mcp.annotations.McpTool;
import io.github.abdulhaseeb22.mcp.core.McpToolDefinition;
import io.github.abdulhaseeb22.mcp.core.McpToolDefinition.McpParamDefinition;
import io.github.abdulhaseeb22.mcp.core.McpToolRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit tests for {@link McpToolScanner} — no Spring context.
 *
 * <p>The scanner and registry are instantiated directly and
 * {@code postProcessAfterInitialization} is invoked manually so tests run
 * fast and remain independent of the Spring lifecycle.
 */
class McpToolScannerTest {

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    @McpServer(name = "test-server", description = "Server used in unit tests")
    static class DummyServer {

        @McpTool(name = "greet", description = "Greets a person by name")
        public String greet(
                @McpParam(name = "person", description = "Name of the person to greet") String person) {
            return "Hello, " + person;
        }

        @McpTool(name = "add", description = "Adds two integers together")
        public int add(
                @McpParam(name = "a", description = "First operand",  required = true)  int a,
                @McpParam(name = "b", description = "Second operand", required = false) int b) {
            return a + b;
        }
    }

    /** A plain Spring bean with no {@code @McpServer} — scanner must ignore it. */
    static class PlainBean {
        public String doWork() { return "done"; }
    }

    // -------------------------------------------------------------------------
    // SUT
    // -------------------------------------------------------------------------

    McpToolRegistry registry;
    McpToolScanner  scanner;

    @BeforeEach
    void setUp() {
        registry = new McpToolRegistry();
        scanner  = new McpToolScanner(registry);
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    void registersAllToolsFromMcpServerBean() {
        scanner.postProcessAfterInitialization(new DummyServer(), "dummyServer");

        assertThat(registry.getAllTools()).hasSize(2);
    }

    @Test
    void toolNamesAndDescriptionsAreCorrect() {
        scanner.postProcessAfterInitialization(new DummyServer(), "dummyServer");

        McpToolDefinition greet = registry.findTool("greet").orElseThrow();
        assertThat(greet.name()).isEqualTo("greet");
        assertThat(greet.description()).isEqualTo("Greets a person by name");

        McpToolDefinition add = registry.findTool("add").orElseThrow();
        assertThat(add.name()).isEqualTo("add");
        assertThat(add.description()).isEqualTo("Adds two integers together");
    }

    @Test
    void paramsAreMappedCorrectly() {
        scanner.postProcessAfterInitialization(new DummyServer(), "dummyServer");

        List<McpParamDefinition> params = registry.findTool("add").orElseThrow().params();
        assertThat(params).hasSize(2);

        McpParamDefinition a = params.get(0);
        assertThat(a.name()).isEqualTo("a");
        assertThat(a.description()).isEqualTo("First operand");
        assertThat(a.required()).isTrue();
        assertThat(a.type()).isEqualTo(int.class);

        McpParamDefinition b = params.get(1);
        assertThat(b.name()).isEqualTo("b");
        assertThat(b.description()).isEqualTo("Second operand");
        assertThat(b.required()).isFalse();
        assertThat(b.type()).isEqualTo(int.class);
    }

    @Test
    void nonMcpServerBeanLeavesRegistryEmpty() {
        scanner.postProcessAfterInitialization(new PlainBean(), "plainBean");

        assertThat(registry.getAllTools()).isEmpty();
    }
}
