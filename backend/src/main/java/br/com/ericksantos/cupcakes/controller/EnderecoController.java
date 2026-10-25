package br.com.ericksantos.cupcakes.controller;

import br.com.ericksantos.cupcakes.dto.EnderecoRequest;
import br.com.ericksantos.cupcakes.dto.EnderecoResponse;
import br.com.ericksantos.cupcakes.service.EnderecoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/enderecos")
@RequiredArgsConstructor
public class EnderecoController {

    private final EnderecoService enderecoService;

    @GetMapping
    public List<EnderecoResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return enderecoService.listar(jwt.getSubject());
    }

    @PostMapping
    public ResponseEntity<EnderecoResponse> criar(@AuthenticationPrincipal Jwt jwt,
                                                  @RequestBody @Valid EnderecoRequest request) {
        EnderecoResponse criado = enderecoService.criar(jwt.getSubject(), request);
        return ResponseEntity.created(URI.create("/api/enderecos/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    public EnderecoResponse atualizar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
                                      @RequestBody @Valid EnderecoRequest request) {
        return enderecoService.atualizar(jwt.getSubject(), id, request);
    }

    @PatchMapping("/{id}/padrao")
    public EnderecoResponse definirPadrao(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return enderecoService.definirPadrao(jwt.getSubject(), id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        enderecoService.excluir(jwt.getSubject(), id);
        return ResponseEntity.noContent().build();
    }
}
