package com.northstar.crm.exception;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The client payload. Six fields, the same six on every failure, so a consumer
 * parses one shape and switches on status and error rather than on prose.
 *
 * errors is always present. An empty object is a value the client can read;
 * a missing key is a branch the client has to write.
 */
public class ErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String correlationId;
    private final Map<String, String> errors;

    public ErrorResponse(int status, String error, String message,
                         String correlationId, Map<String, String> errors) {
        this(Instant.now(), status, error, message, correlationId, errors);
    }

    /** Timestamp is a parameter here so a test can assert the JSON shape. */
    public ErrorResponse(Instant timestamp, int status, String error, String message,
                         String correlationId, Map<String, String> errors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.correlationId = correlationId;
        // LinkedHashMap, not Map.copyOf: the field order in the JSON has to be
        // the order the violations were collected in, run after run.
        this.errors = errors == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(errors));
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    /**
     * There is no JSON library on this classpath, so the fields are written out
     * here. Every string goes through escape, because a message that carries a
     * quote or a newline would otherwise break the document.
     */
    public String toJson() {
        StringBuilder json = new StringBuilder(160);
        json.append("{\"timestamp\":").append(quote(timestamp == null ? null : timestamp.toString()))
                .append(",\"status\":").append(status)
                .append(",\"error\":").append(quote(error))
                .append(",\"message\":").append(quote(message))
                .append(",\"correlationId\":").append(quote(correlationId))
                .append(",\"errors\":{");
        boolean first = true;
        for (Map.Entry<String, String> field : errors.entrySet()) {
            if (!first) {
                json.append(',');
            }
            json.append(quote(field.getKey())).append(':').append(quote(field.getValue()));
            first = false;
        }
        return json.append("}}").toString();
    }

    private static String quote(String value) {
        return value == null ? "null" : "\"" + escape(value) + "\"";
    }

    private static String escape(String value) {
        StringBuilder out = new StringBuilder(value.length() + 8);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }

    @Override
    public String toString() {
        return toJson();
    }
}
