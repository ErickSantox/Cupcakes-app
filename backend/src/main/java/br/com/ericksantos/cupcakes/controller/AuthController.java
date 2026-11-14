package br.com.ericksantos.cupcakes.controller;

import br.com.ericksantos.cupcakes.dto.CadastroRequest;
import br.com.ericksantos.cupcakes.dto.EsqueciSenhaRequest;
import br.com.ericksantos.cupcakes.dto.LoginRequest;
import br.com.ericksantos.cupcakes.dto.LoginResponse;
import br.com.ericksantos.cupcakes.dto.RedefinirSenhaRequest;
import br.com.ericksantos.cupcakes.dto.UsuarioResponse;
import br.com.ericksantos.cupcakes.service.AuthService;
import br.com.ericksantos.cupcakes.service.RecuperacaoSenhaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RecuperacaoSenhaService recuperacaoSenhaService;

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastro(@RequestBody @Valid CadastroRequest request){
        return authService.cadastrar(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** Sempre 204, exista ou não o e-mail (não revela quem tem conta) */
    @PostMapping("/esqueci-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void esqueciSenha(@Valid @RequestBody EsqueciSenhaRequest request) {
        recuperacaoSenhaService.solicitar(request.email());
    }

    @PostMapping("/redefinir-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void redefinirSenha(@Valid @RequestBody RedefinirSenhaRequest request) {
        recuperacaoSenhaService.redefinir(request.token(), request.novaSenha());
    }

    @GetMapping("/me")
    public UsuarioResponse me(@AuthenticationPrincipal Jwt jwt) {
        return authService.buscarPorEmail(jwt.getSubject());
    }
}
