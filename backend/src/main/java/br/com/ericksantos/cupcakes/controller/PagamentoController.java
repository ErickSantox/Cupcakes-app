package br.com.ericksantos.cupcakes.controller;

import br.com.ericksantos.cupcakes.dto.PagamentoCartaoRequest;
import br.com.ericksantos.cupcakes.dto.PagamentoResponse;
import br.com.ericksantos.cupcakes.service.PagamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos/{pedidoId}/pagamento")
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService pagamentoService;

    @PostMapping("/cartao")
    public ResponseEntity<PagamentoResponse> cartao (@AuthenticationPrincipal Jwt jwt, @PathVariable Long pedidoId, @RequestBody @Valid PagamentoCartaoRequest req){
        PagamentoResponse cartao = pagamentoService.pagarComCartao(jwt.getSubject(),pedidoId,req);
        return ResponseEntity.ok(cartao);
    }
}
