package com.atypon.bootstrap.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {
    @JsonProperty("username")
    private String username;
    @JsonProperty("password")
    private String password;
}
