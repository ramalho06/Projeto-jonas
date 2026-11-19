package com.example.jonas_project.usuario.dto;

import com.example.jonas_project.usuario.entity.PerfilUsuario;

public record UsuarioResponse(Long id, String nome, String email, PerfilUsuario perfil) {
}
