package io.github.abdulhaseeb22.mcp.core.jsonrpc;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the {@code error} object in a JSON-RPC 2.0 response.
 *
 * <p>Standard error codes defined by the JSON-RPC spec:
 * <ul>
 *   <li>{@code -32700} – Parse error</li>
 *   <li>{@code -32600} – Invalid request</li>
 *   <li>{@code -32601} – Method not found</li>
 *   <li>{@code -32602} – Invalid params</li>
 *   <li>{@code -32603} – Internal error</li>
 * </ul>
 *
 * @param code    Numeric error code.
 * @param message Short human-readable description of the error.
 */
public record JsonRpcError(
        @JsonProperty("code")    int    code,
        @JsonProperty("message") String message
) {

    /** Parse error — the JSON received could not be parsed. */
    public static final int PARSE_ERROR      = -32700;
    /** Invalid request — the JSON is valid but is not a valid JSON-RPC request. */
    public static final int INVALID_REQUEST  = -32600;
    /** Method not found — the requested method does not exist. */
    public static final int METHOD_NOT_FOUND = -32601;
    /** Invalid params — the method was found but the parameters are invalid. */
    public static final int INVALID_PARAMS   = -32602;
    /** Internal error — an internal error occurred while processing the request. */
    public static final int INTERNAL_ERROR   = -32603;

    /** Convenience factory for a {@link #METHOD_NOT_FOUND} error. */
    public static JsonRpcError methodNotFound(String name) {
        return new JsonRpcError(METHOD_NOT_FOUND, "Method not found: " + name);
    }

    /** Convenience factory for an {@link #INVALID_PARAMS} error. */
    public static JsonRpcError invalidParams(String detail) {
        return new JsonRpcError(INVALID_PARAMS, "Invalid params: " + detail);
    }

    /** Convenience factory for an {@link #INTERNAL_ERROR} error. */
    public static JsonRpcError internalError(String detail) {
        return new JsonRpcError(INTERNAL_ERROR, "Internal error: " + detail);
    }
}