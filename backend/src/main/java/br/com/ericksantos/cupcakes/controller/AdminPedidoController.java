package br.com.ericksantos.cupcakes.controller;

import br.com.ericksantos.cupcakes.dto.AtualizarStatusRequest;
import br.com.ericksantos.cupcakes.dto.PedidoResponse;
import br.com.ericksantos.cupcakes.model.enums.StatusPedido;
import br.com.ericksantos.cupcakes.service.AdminPedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/pedidos")
@RequiredArgsConstructor
public class AdminPedidoController {

    private final AdminPedidoService adminPedidoService;

    @PatchMapping("/{id}/status")
    public PedidoResponse atualizarStatus(@PathVariable Long id, @RequestBody @Valid AtualizarStatusRequest request) {
        return adminPedidoService.atualizarStatus(id, request.status());
    }

    @GetMapping
    public PagedModel<PedidoResponse> listar(
        @RequestParam(required = false) StatusPedido status,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
        @PageableDefault(size = 20, sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(adminPedidoService.listar(status, de, ate, pageable));
    }

    @GetMapping("/{id}")
    public PedidoResponse buscar(@PathVariable Long id) {
        return adminPedidoService.buscar(id);
    }

}
