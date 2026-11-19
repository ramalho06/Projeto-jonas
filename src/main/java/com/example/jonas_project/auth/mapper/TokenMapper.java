package com.example.jonas_project.auth.mapper;

import com.example.jonas_project.auth.dto.TokenResponse;
import com.example.jonas_project.auth.service.AutenticacaoService;

public final class TokenMapper {
    private TokenMapper() {
    }

    public static TokenResponse paraResponse(String token) {
        return new TokenResponse(token, "Bearer", AutenticacaoService.VALIDADE_TOKEN.toSeconds());
    }
}
