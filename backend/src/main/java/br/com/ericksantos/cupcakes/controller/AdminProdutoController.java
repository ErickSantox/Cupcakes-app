package br.com.ericksantos.cupcakes.controller;

import br.com.ericksantos.cupcakes.dto.AtivoRequest;
import br.com.ericksantos.cupcakes.dto.ProdutoRequest;
import br.com.ericksantos.cupcakes.dto.ProdutoResponse;
import br.com.ericksantos.cupcakes.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/admin/produtos")
@RequiredArgsConstructor
public class AdminProdutoController {

    private final ProdutoService produtoService;

    @GetMapping
    public List<ProdutoResponse> listar() {
        return produtoService.listarTodos();
    }

    @PostMapping
    public ResponseEntity<ProdutoResponse> criar(@RequestBody @Valid ProdutoRequest req) {
        ProdutoResponse criado = produtoService.criar(req);
        return ResponseEntity.created(URI.create("/api/produtos/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    public ProdutoResponse atualizar(@PathVariable Long id, @RequestBody @Valid ProdutoRequest request) {
        return produtoService.atualizar(id, request);
    }

    @PatchMapping("/{id}/ativo")
    public ProdutoResponse alterarAtivo(@PathVariable Long id, @RequestBody @Valid AtivoRequest request) {
        return produtoService.alterarAtivo(id, request.ativo());
    }
}
