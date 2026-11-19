package com.example.jonas_project.auth.dto;

public record TokenResponse(String token, String tipo, long expiraEmSegundos) {
}
