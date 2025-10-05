package com.soilidstate.api.dto;

import lombok.Data;

@Data
public class ConnectionStatusResponse {
    private boolean connected;
    private String serverAddress;
    private Integer port;
    private String message;
    private Long connectedAt;
}
