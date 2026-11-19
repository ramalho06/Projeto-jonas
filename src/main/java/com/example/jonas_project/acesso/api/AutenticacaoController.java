package com.example.jonas_project.acesso.api;

import com.example.jonas_project.acesso.api.dto.CadastroRequest;
import com.example.jonas_project.acesso.api.dto.LoginRequest;
import com.example.jonas_project.acesso.api.dto.TokenResponse;
import com.example.jonas_project.acesso.api.dto.UsuarioResponse;
import com.example.jonas_project.acesso.api.mapper.TokenMapper;
import com.example.jonas_project.acesso.api.mapper.UsuarioMapper;
import com.example.jonas_project.acesso.aplicacao.AutenticacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AutenticacaoController {
    private final AutenticacaoService autenticacao;

    public AutenticacaoController(AutenticacaoService autenticacao) {
        this.autenticacao = autenticacao;
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrar(@RequestBody CadastroRequest requisicao) {
        return UsuarioMapper.paraResponse(
                autenticacao.cadastrar(requisicao.nome(), requisicao.email(), requisicao.senha()));
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest requisicao) {
        return TokenMapper.paraResponse(autenticacao.login(requisicao.email(), requisicao.senha()));
    }
}
