package com.example.jonas_project.acesso.api.mapper;

import com.example.jonas_project.acesso.api.dto.UsuarioResponse;
import com.example.jonas_project.acesso.dominio.Usuario;

public final class UsuarioMapper {
    private UsuarioMapper() {
    }

    public static UsuarioResponse paraResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil());
    }
}
