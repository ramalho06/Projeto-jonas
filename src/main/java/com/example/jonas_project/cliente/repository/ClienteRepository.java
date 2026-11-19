package com.example.jonas_project.cliente.repository;

import com.example.jonas_project.cliente.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
