package br.com.ericksantos.cupcakes.controller;

import br.com.ericksantos.cupcakes.dto.FreteResponse;
import br.com.ericksantos.cupcakes.service.FreteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/frete")
@RequiredArgsConstructor

public class FreteController {

    private final FreteService freteService;

    @GetMapping
    public FreteResponse calcularFrete(@RequestParam String cep, @RequestParam BigDecimal subtotal) {
        return freteService.calcular(cep,subtotal);
    }
}
