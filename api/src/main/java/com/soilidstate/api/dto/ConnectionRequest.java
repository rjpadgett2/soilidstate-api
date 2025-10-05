package com.soilidstate.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConnectionRequest {
    @NotBlank(message = "Server address is required")
    private String serverAddress;

    @NotNull(message = "Port is required")
    private Integer port = 5661;

    private String password;
}