package br.com.ericksantos.cupcakes.controller;

import br.com.ericksantos.cupcakes.dto.PedidoRequest;
import br.com.ericksantos.cupcakes.dto.PedidoResponse;
import br.com.ericksantos.cupcakes.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<PedidoResponse> salvar(@AuthenticationPrincipal Jwt jwt,
                                                 @Valid @RequestBody PedidoRequest pedidoRequest){
        PedidoResponse salvo = pedidoService.criar(jwt.getSubject(),pedidoRequest);
        return ResponseEntity.created(URI.create("/api/pedidos/" + salvo.id())).body(salvo);
    }

    @GetMapping("/{id}")
    public PedidoResponse buscar(@AuthenticationPrincipal Jwt jwt,@PathVariable Long id){
        return pedidoService.buscar(jwt.getSubject(),id);
    }

}
