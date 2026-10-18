package br.com.ericksantos.cupcakes.service;

import br.com.ericksantos.cupcakes.dto.CategoriaResponse;
import br.com.ericksantos.cupcakes.dto.ProdutoResponse;
import br.com.ericksantos.cupcakes.exception.NaoEncontradoException;
import br.com.ericksantos.cupcakes.model.Categoria;
import br.com.ericksantos.cupcakes.model.Produto;
import br.com.ericksantos.cupcakes.repository.CategoriaRepository;
import br.com.ericksantos.cupcakes.repository.ProdutoRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    private final CategoriaRepository categoriaRepository;


    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar(Long categoriaId) {
        List<Produto>  produtos;

        if (categoriaId == null) {
            produtos = produtoRepository.findByAtivoTrueOrderByNomeAsc();
            return produtos.stream().map(ProdutoResponse::de).toList();
        }

        produtos = produtoRepository.findByAtivoTrueAndCategoriaIdOrderByNomeAsc(categoriaId);

        return produtos.stream().map(ProdutoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscar(Long id) {
        return produtoRepository.findByIdAndAtivoTrue(id).map(ProdutoResponse::de).orElseThrow(() -> new NaoEncontradoException("Produto não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarCategorias() {
        List<Categoria> categorias =  categoriaRepository.findAll(Sort.by("nome"));

        return  categorias.stream().map(CategoriaResponse::de).toList();
    }


}
