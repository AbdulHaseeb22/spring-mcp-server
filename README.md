# spring-mcp-server

[![Build](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Java](https://img.shields.io/badge/Java-17+-orange)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3+-green)]()
[![License](https://img.shields.io/badge/license-Apache%202.0-blue)]()

**Annotation-driven Spring Boot Starter that exposes your Spring services as [Model Context Protocol (MCP)](https://modelcontextprotocol.io) tools — so AI agents like Claude, Cursor, and ChatGPT can call your real services directly.**

Think `@RestController` but for AI agents.

---

## The Problem

Connecting an AI agent to your Spring Boot app today means manually implementing JSON-RPC 2.0, writing MCP-compliant HTTP endpoints, handling tool registration, parameter coercion, and error codes — 300–500 lines of boilerplate per project.

## The Solution

Add one dependency. Write three annotations. Done.

```java
@Service
@McpServer(description = "Order management service")
public class OrderService {

    @McpTool(description = "Find orders by customer ID")
    public List<Order> getOrders(
            @McpParam(description = "The customer ID") String customerId) {
        return orderRepo.findByCustomer(customerId);
    }
}
```

Your service is now an MCP server. Any AI agent can discover and call `getOrders` — no protocol code, no boilerplate.

---

## How It Works
Spring startup → McpToolScanner scans @McpServer beans
→ registers @McpTool methods in McpToolRegistry
AI Agent → POST /mcp {"method": "tools/list"}
→ returns all tools with JSON Schema
AI Agent → POST /mcp {"method": "tools/call", "params": {"name": "getOrders", ...}}
→ McpRequestHandler invokes method via reflection
→ returns result as MCP content

---

## Quick Start

### 1. Add the dependency

```xml
<dependency>
    <groupId>io.github.abdulhaseeb22</groupId>
    <artifactId>spring-mcp-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. Annotate your service

```java
@Service
@McpServer(name = "bookstore", description = "Bookstore service")
public class BookstoreService {

    @McpTool(description = "Search books by title, author or category")
    public List<Book> searchBooks(
            @McpParam(description = "Search term") String query) {
        // your existing logic
    }

    @McpTool(description = "Get inventory count for a category")
    public int getInventoryCount(
            @McpParam(description = "Category name") String category) {
        // your existing logic
    }
}
```

### 3. Run your app

```bash
mvn spring-boot:run
```

That's it. Your MCP server is live.

---

## Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/mcp/health` | Health check |
| `POST` | `/mcp` | MCP JSON-RPC endpoint |

### tools/list

```bash
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":1,"method":"tools/list","params":{}}'
```

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "tools": [
      {
        "name": "searchBooks",
        "description": "Search books by title, author or category",
        "inputSchema": {
          "type": "object",
          "properties": {
            "query": { "type": "string", "description": "Search term" }
          },
          "required": ["query"]
        }
      }
    ]
  }
}
```

### tools/call

```bash
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 2,
    "method": "tools/call",
    "params": {
      "name": "searchBooks",
      "arguments": { "query": "clean code" }
    }
  }'
```

---

## Annotations

### `@McpServer`
Marks a Spring bean as an MCP server. Place on any `@Service`, `@Component`, or `@Bean`.

| Attribute | Description | Default |
|-----------|-------------|---------|
| `name` | Server name | Class name |
| `description` | Human-readable description | `""` |

### `@McpTool`
Marks a method as an MCP tool callable by AI agents.

| Attribute | Description | Default |
|-----------|-------------|---------|
| `name` | Tool name exposed to AI | Method name |
| `description` | What this tool does (**required**) | — |

### `@McpParam`
Describes a method parameter for the AI agent.

| Attribute | Description | Default |
|-----------|-------------|---------|
| `name` | Parameter name | Parameter name |
| `description` | What this parameter is | `""` |
| `required` | Whether required | `true` |

---

## Supported Return Types

| Java Type | JSON Schema Type |
|-----------|-----------------|
| `String` | `string` |
| `int` / `Integer` / `long` | `integer` |
| `double` / `float` | `number` |
| `boolean` / `Boolean` | `boolean` |
| Any object / List / Map | Serialized as JSON via Jackson |

---

## Project Structure
spring-mcp-server/
├── spring-mcp-annotations/      # @McpServer, @McpTool, @McpParam
├── spring-mcp-core/             # JSON-RPC models, McpToolRegistry
├── spring-mcp-autoconfigure/    # Bean scanner, request handler, controller
├── spring-mcp-starter/          # Starter (add this to your project)
└── spring-mcp-sample/           # Demo bookstore app

---

## Running the Sample App

```bash
git clone https://github.com/AbdulHaseeb22/spring-mcp-server.git
cd spring-mcp-server
mvn install -DskipTests
cd spring-mcp-sample
mvn spring-boot:run
```

Then hit `http://localhost:8080/mcp/health` to confirm it's up.

---

## Requirements

- Java 17+
- Spring Boot 3.x

---

## License

Apache License 2.0
