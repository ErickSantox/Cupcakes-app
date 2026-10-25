package br.com.ericksantos.cupcakes.dto;

import br.com.ericksantos.cupcakes.model.Endereco;

public record EnderecoResponse(Long id, String cep, String logradouro, String numero, String complemento,
                               String bairro, String cidade, String uf, boolean padrao) {

    public static EnderecoResponse de(Endereco e) {
        return new EnderecoResponse(e.getId(), e.getCep(), e.getLogradouro(), e.getNumero(), e.getComplemento(),
            e.getBairro(), e.getCidade(), e.getUf(), e.getPadrao());
    }
}
