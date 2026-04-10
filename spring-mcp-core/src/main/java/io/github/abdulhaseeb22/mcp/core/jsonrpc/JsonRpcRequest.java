package io.github.abdulhaseeb22.mcp.core.jsonrpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Represents an incoming JSON-RPC 2.0 request as defined by the
 * <a href="https://www.jsonrpc.org/specification">JSON-RPC 2.0 specification</a>.
 *
 * <p>{@code @JsonIgnoreProperties(ignoreUnknown = true)} is applied so that
 * future protocol extensions (e.g. extra fields) do not cause deserialization
 * failures.
 *
 * @param jsonrpc Must be {@code "2.0"} per the JSON-RPC spec.
 * @param method  The MCP method name, e.g. {@code "tools/list"} or
 *                {@code "tools/call"}.
 * @param id      Request identifier (string, number, or {@code null} for
 *                notifications). Preserved verbatim in the response.
 * @param params  Named parameters map. May be {@code null} for methods that
 *                require no arguments.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record JsonRpcRequest(
        @JsonProperty("jsonrpc") String              jsonrpc,
        @JsonProperty("method")  String              method,
        @JsonProperty("id")      Object              id,
        @JsonProperty("params")  Map<String, Object> params
) {

    /**
     * Returns {@code true} if this message is a JSON-RPC notification,
     * i.e. it has no {@code id} and therefore expects no response.
     */
    public boolean isNotification() {
        return id == null;
    }

    /**
     * Retrieves a named parameter value, or {@code null} if absent.
     *
     * @param key the parameter key
     * @return the parameter value, or {@code null}
     */
    public Object param(String key) {
        return params != null ? params.get(key) : null;
    }
}