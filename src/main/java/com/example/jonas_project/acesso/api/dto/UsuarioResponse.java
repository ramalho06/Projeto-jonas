package com.example.jonas_project.acesso.api.dto;

import com.example.jonas_project.acesso.dominio.PerfilUsuario;

public record UsuarioResponse(Long id, String nome, String email, PerfilUsuario perfil) {
}
