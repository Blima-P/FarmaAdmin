package br.com.farmaadmin.controller;

import br.com.farmaadmin.dao.UsuarioDAO;
import br.com.farmaadmin.modelo.Usuario;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.sql.SQLException;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario loginRequest) {
        try {
            Usuario usuarioLogado = usuarioDAO.buscarPorEmailESenha(
                    loginRequest.getEmail(),
                    loginRequest.getSenha()
            );

            if (usuarioLogado != null) {
                return ResponseEntity.ok(usuarioLogado);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou senha inválidos.");
            }
        } catch (SQLException e) {
            System.err.println("Erro SQL no login: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro no servidor: Falha de comunicação com o banco de dados.");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Usuario novoUsuario) {
        try {
            novoUsuario.setTipoUsuario("CLIENTE");
            Usuario usuarioCriado = usuarioDAO.adicionar(novoUsuario);

            if (usuarioCriado != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(usuarioCriado);
            }
            return ResponseEntity.badRequest().body("Falha ao registrar usuário.");

        } catch (SQLException e) {
            System.err.println("Erro SQL no registro: " + e.getMessage());
            if (e.getMessage().contains("Duplicate entry") || e.getMessage().contains("UNIQUE constraint failed")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email já cadastrado.");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao criar usuário: " + e.getMessage());
        }
    }
}
