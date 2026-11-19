package com.example.jonas_project.auth.service;

import com.example.jonas_project.common.exception.ValidacaoDominio;
import com.example.jonas_project.usuario.entity.PerfilUsuario;
import com.example.jonas_project.usuario.entity.Usuario;
import com.example.jonas_project.usuario.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import static com.example.jonas_project.common.exception.ValidacaoDominio.texto;

@Service
public class AutenticacaoService {
    public static final Duration VALIDADE_TOKEN = Duration.ofHours(1);
    private static final int TAMANHO_MINIMO_SENHA = 8;

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    public AutenticacaoService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    /** Cadastro público: sempre cria conta de perfil CLIENTE, sem vínculo com um Cliente. */
    @Transactional
    public Usuario cadastrar(String nome, String email, String senha) {
        if (senha == null || senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new IllegalArgumentException("A senha deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres.");
        }
        if (usuarios.existsByEmail(normalizarEmail(email))) {
            throw emailJaCadastrado();
        }
        try {
            // flush aqui para a violação do unique de e-mail (cadastros simultâneos) estourar dentro do try
            return usuarios.saveAndFlush(new Usuario(nome, email, passwordEncoder.encode(senha), PerfilUsuario.CLIENTE));
        } catch (DataIntegrityViolationException corrida) {
            throw emailJaCadastrado();
        }
    }

    private static ResponseStatusException emailJaCadastrado() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado.");
    }

    @Transactional(readOnly = true)
    public String login(String email, String senha) {
        Usuario usuario = usuarios.findByEmail(normalizarEmail(email))
                .filter(Usuario::isAtivo)
                .filter(u -> senha != null && passwordEncoder.matches(senha, u.getSenhaCodificada()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos."));

        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(usuario.getId().toString())
                .issuedAt(agora)
                .expiresAt(agora.plus(VALIDADE_TOKEN))
                .claim("scope", usuario.getPerfil().name())
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    private static String normalizarEmail(String email) {
        return texto(email, "o e-mail").toLowerCase(Locale.ROOT);
    }
}
