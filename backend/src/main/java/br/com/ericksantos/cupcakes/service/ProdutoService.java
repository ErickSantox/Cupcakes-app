package br.com.ericksantos.cupcakes.service;

import br.com.ericksantos.cupcakes.dto.CategoriaResponse;
import br.com.ericksantos.cupcakes.dto.ProdutoRequest;
import br.com.ericksantos.cupcakes.dto.ProdutoResponse;
import br.com.ericksantos.cupcakes.exception.NaoEncontradoException;
import br.com.ericksantos.cupcakes.model.Categoria;
import br.com.ericksantos.cupcakes.model.Produto;
import br.com.ericksantos.cupcakes.repository.CategoriaRepository;
import br.com.ericksantos.cupcakes.repository.ProdutoRepository;
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
    public List<ProdutoResponse> listar(Long categoriaId, String busca) {
        String buscaNormalizada = (busca == null || busca.isBlank()) ? null : busca.trim();

        List<Produto> produtos = produtoRepository.buscarVitrine(categoriaId, buscaNormalizada);

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


    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarTodos() {
        return produtoRepository.findAllByOrderByNomeAsc()
            .stream()
            .map(ProdutoResponse::de)
            .toList();
    }

    @Transactional
    public ProdutoResponse criar(ProdutoRequest req) {
        Produto produto = new Produto();
        preencher(produto, req);
        return ProdutoResponse.de(produtoRepository.save(produto));
    }

    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoRequest request) {
        Produto produto = buscarEntidade(id);
        preencher(produto, request);
        return ProdutoResponse.de(produto);
    }

    @Transactional
    public ProdutoResponse alterarAtivo(Long id, boolean ativo) {
        Produto produto = buscarEntidade(id);
        produto.setAtivo(ativo);
        return ProdutoResponse.de(produto);
    }

    private Produto buscarEntidade(Long id) {
        return produtoRepository.findById(id)
            .orElseThrow(() -> new NaoEncontradoException("Produto não encontrado."));
    }




    private void preencher(Produto produto, ProdutoRequest req) {
        Categoria categoria = categoriaRepository.findById(req.categoriaId())
            .orElseThrow(() -> new NaoEncontradoException("Categoria nao encontrada"));

        produto.setNome(req.nome().trim());
        produto.setDescricao(req.descricao().trim());
        produto.setPreco(req.preco());
        produto.setCategoria(categoria);
        produto.setFotoUrl(req.fotoUrl().trim());
        produto.setEstoque(req.estoque());
    }

}
