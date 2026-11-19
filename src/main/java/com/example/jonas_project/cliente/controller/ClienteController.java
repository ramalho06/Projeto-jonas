package com.example.jonas_project.cliente.controller;

import com.example.jonas_project.cliente.dto.ClienteResponse;
import com.example.jonas_project.cliente.mapper.ClienteMapper;
import com.example.jonas_project.cliente.service.ClienteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
    private final ClienteService clientes;

    public ClienteController(ClienteService clientes) {
        this.clientes = clientes;
    }

    @GetMapping("/listar")
    public List<ClienteResponse> listar() {
        return clientes.listar().stream()
                .map(ClienteMapper::paraResponse)
                .toList();
    }
}
