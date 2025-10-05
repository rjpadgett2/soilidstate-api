package com.soilidstate.api.dto;

import lombok.Data;

@Data
public class ErrorResponse {
    private String error;
    private String message;
    private Long timestamp;

    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
}
