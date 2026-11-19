package com.example.jonas_project.usuario.mapper;

import com.example.jonas_project.usuario.dto.UsuarioResponse;
import com.example.jonas_project.usuario.entity.Usuario;

public final class UsuarioMapper {
    private UsuarioMapper() {
    }

    public static UsuarioResponse paraResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil());
    }
}
