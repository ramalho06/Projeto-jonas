package com.example.jonas_project.acesso.api.dto;

public record TokenResponse(String token, String tipo, long expiraEmSegundos) {
}
