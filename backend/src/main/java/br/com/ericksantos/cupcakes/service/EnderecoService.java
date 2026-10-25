package br.com.ericksantos.cupcakes.service;

import br.com.ericksantos.cupcakes.dto.EnderecoRequest;
import br.com.ericksantos.cupcakes.dto.EnderecoResponse;
import br.com.ericksantos.cupcakes.exception.NaoEncontradoException;
import br.com.ericksantos.cupcakes.exception.RegraNegocioException;
import br.com.ericksantos.cupcakes.model.Endereco;
import br.com.ericksantos.cupcakes.model.Usuario;
import br.com.ericksantos.cupcakes.repository.EnderecoRepository;
import br.com.ericksantos.cupcakes.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnderecoService {

    /** RN#1: máximo de endereços por conta */
    static final int MAX_ENDERECOS = 5;

    private final EnderecoRepository enderecoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<EnderecoResponse> listar(String email) {
        return enderecoRepository.findByUsuarioEmailOrderByPadraoDescIdAsc(email)
            .stream()
            .map(EnderecoResponse::de)
            .toList();
    }

    @Transactional
    public EnderecoResponse criar(String email, EnderecoRequest req) {
        long quantidade = enderecoRepository.countByUsuarioEmail(email);
        if (quantidade >= MAX_ENDERECOS) {
            throw new RegraNegocioException("Você pode cadastrar até " + MAX_ENDERECOS + " endereços.");
        }
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado."));

        Endereco endereco = new Endereco();
        endereco.setUsuario(usuario);
        preencher(endereco, req);

        // O primeiro endereço vira padrão automaticamente
        if (quantidade == 0 || req.padrao()) {
            desmarcarPadraoAtual(email);
            endereco.setPadrao(true);
        }
        return EnderecoResponse.de(enderecoRepository.save(endereco));
    }

    @Transactional
    public EnderecoResponse atualizar(String email, Long id, EnderecoRequest req) {
        Endereco endereco = buscarDoUsuario(email, id);
        preencher(endereco, req);
        if (req.padrao() && !endereco.getPadrao()) {
            desmarcarPadraoAtual(email);
            endereco.setPadrao(true);
        }
        return EnderecoResponse.de(endereco);
    }

    @Transactional
    public EnderecoResponse definirPadrao(String email, Long id) {
        Endereco endereco = buscarDoUsuario(email, id);
        desmarcarPadraoAtual(email);
        endereco.setPadrao(true);
        return EnderecoResponse.de(endereco);
    }

    @Transactional
    public void excluir(String email, Long id) {
        Endereco endereco = buscarDoUsuario(email, id);
        boolean eraPadrao = endereco.getPadrao();
        enderecoRepository.delete(endereco);

        // Se apagou o padrão, promove o mais antigo que sobrou
        if (eraPadrao) {
            enderecoRepository.findFirstByUsuarioEmailOrderByIdAsc(email)
                .ifPresent(outro -> outro.setPadrao(true));
        }
    }

    /** IDOR: endereço de outro usuário responde 404, como se não existisse */
    private Endereco buscarDoUsuario(String email, Long id) {
        return enderecoRepository.findByIdAndUsuarioEmail(id, email)
            .orElseThrow(() -> new NaoEncontradoException("Endereço não encontrado."));
    }

    private void desmarcarPadraoAtual(String email) {
        enderecoRepository.findByUsuarioEmailAndPadraoTrue(email)
            .ifPresent(atual -> atual.setPadrao(false));
    }

    private void preencher(Endereco endereco, EnderecoRequest req) {
        endereco.setCep(req.cep());
        endereco.setLogradouro(req.logradouro().trim());
        endereco.setNumero(req.numero().trim());
        endereco.setComplemento(req.complemento() == null || req.complemento().isBlank() ? null : req.complemento().trim());
        endereco.setBairro(req.bairro().trim());
        endereco.setCidade(req.cidade().trim());
        endereco.setUf(req.uf());
    }
}
