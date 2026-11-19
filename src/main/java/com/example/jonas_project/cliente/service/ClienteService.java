package com.example.jonas_project.cliente.service;

import com.example.jonas_project.cliente.entity.Cliente;
import com.example.jonas_project.cliente.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {
    private final ClienteRepository clientes;

    public ClienteService(ClienteRepository clientes) {
        this.clientes = clientes;
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar() {
        return clientes.findAll();
    }
}
