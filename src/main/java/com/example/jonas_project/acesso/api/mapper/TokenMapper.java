package com.example.jonas_project.acesso.api.mapper;

import com.example.jonas_project.acesso.api.dto.TokenResponse;
import com.example.jonas_project.acesso.aplicacao.AutenticacaoService;

public final class TokenMapper {
    private TokenMapper() {
    }

    public static TokenResponse paraResponse(String token) {
        return new TokenResponse(token, "Bearer", AutenticacaoService.VALIDADE_TOKEN.toSeconds());
    }
}
