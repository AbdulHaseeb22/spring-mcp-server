package io.github.abdulhaseeb22.mcp.core.jsonrpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an outgoing JSON-RPC 2.0 response.
 *
 * <p>Per the spec, exactly one of {@code result} or {@code error} must be
 * present — never both, never neither (for non-notification requests).
 * {@code @JsonInclude(NON_NULL)} ensures the absent field is omitted from
 * the serialized JSON.
 *
 * @param jsonrpc Always {@code "2.0"}.
 * @param id      Mirrors the {@code id} from the originating request,
 *                or {@code null} if the request id could not be determined.
 * @param result  The success payload; {@code null} when {@code error} is set.
 * @param error   The error object; {@code null} when {@code result} is set.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record JsonRpcResponse(
        @JsonProperty("jsonrpc") String       jsonrpc,
        @JsonProperty("id")      Object       id,
        @JsonProperty("result")  Object       result,
        @JsonProperty("error")   JsonRpcError error
) {

    /** The JSON-RPC version string included in every response. */
    private static final String VERSION = "2.0";

    /**
     * Creates a successful response carrying the given result payload.
     *
     * @param id     the request id to echo back
     * @param result the success payload (may be any Jackson-serializable object)
     * @return a response with {@code error} set to {@code null}
     */
    public static JsonRpcResponse success(Object id, Object result) {
        return new JsonRpcResponse(VERSION, id, result, null);
    }

    /**
     * Creates an error response.
     *
     * @param id    the request id to echo back ({@code null} if unknown)
     * @param error the error descriptor
     * @return a response with {@code result} set to {@code null}
     */
    public static JsonRpcResponse error(Object id, JsonRpcError error) {
        return new JsonRpcResponse(VERSION, id, null, error);
    }
}