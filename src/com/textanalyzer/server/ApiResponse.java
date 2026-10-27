package com.textanalyzer.server;

import com.textanalyzer.util.JsonUtil;

/**
 * Standard API response wrapper for JSON responses.
 * Demonstrates standard REST response modeling and Encapsulation.
 */
public class ApiResponse {

    private final boolean success;
    private final String message;
    private final String dataJson;

    private ApiResponse(boolean success, String message, String dataJson) {
        this.success = success;
        this.message = message != null ? message : "";
        this.dataJson = dataJson != null ? dataJson : "null";
    }

    public static ApiResponse ok(String message, String dataJson) {
        return new ApiResponse(true, message, dataJson);
    }

    public static ApiResponse ok(String dataJson) {
        return new ApiResponse(true, "Operation successful", dataJson);
    }

    public static ApiResponse error(String message) {
        return new ApiResponse(false, message, "null");
    }

    public String toJson() {
        return String.format(
                "{\"success\":%b,\"message\":\"%s\",\"data\":%s}",
                success,
                JsonUtil.escape(message),
                dataJson
        );
    }
}
