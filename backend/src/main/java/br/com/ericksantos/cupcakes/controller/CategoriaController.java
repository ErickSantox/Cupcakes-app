package br.com.ericksantos.cupcakes.controller;

import br.com.ericksantos.cupcakes.dto.CategoriaResponse;
import br.com.ericksantos.cupcakes.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final ProdutoService produtoService;




    @GetMapping()
    public List<CategoriaResponse> listar(){
        return produtoService.listarCategorias();
    }

}
