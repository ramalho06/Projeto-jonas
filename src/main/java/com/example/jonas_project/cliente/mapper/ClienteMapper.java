package com.example.jonas_project.cliente.mapper;

import com.example.jonas_project.cliente.dto.ClienteResponse;
import com.example.jonas_project.cliente.entity.Cliente;

public final class ClienteMapper {
    private ClienteMapper() {
    }

    public static ClienteResponse paraResponse(Cliente cliente) {
        return new ClienteResponse(cliente.getId(), cliente.getNome(), cliente.getContato());
    }
}
