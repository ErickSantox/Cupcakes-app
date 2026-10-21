package br.com.ericksantos.cupcakes.controller;

import br.com.ericksantos.cupcakes.dto.ProdutoResponse;
import br.com.ericksantos.cupcakes.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    @GetMapping()
    public List<ProdutoResponse> listar(@RequestParam(required = false) Long categoriaId,
                                        @RequestParam(required= false) String busca){
        return produtoService.listar(categoriaId,busca);
    }

    @GetMapping("/{id}")
    public ProdutoResponse buscarPorId(@PathVariable Long id) {
        return produtoService.buscar(id);
    }




}
