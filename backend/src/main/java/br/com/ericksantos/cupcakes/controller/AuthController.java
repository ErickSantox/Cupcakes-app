package br.com.ericksantos.cupcakes.controller;

import br.com.ericksantos.cupcakes.dto.CadastroRequest;
import br.com.ericksantos.cupcakes.dto.LoginRequest;
import br.com.ericksantos.cupcakes.dto.LoginResponse;
import br.com.ericksantos.cupcakes.dto.UsuarioResponse;
import br.com.ericksantos.cupcakes.service.AuthService;
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

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastro(@RequestBody @Valid CadastroRequest request){
        return authService.cadastrar(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UsuarioResponse me(@AuthenticationPrincipal Jwt jwt) {
        return authService.buscarPorEmail(jwt.getSubject());
    }
}
