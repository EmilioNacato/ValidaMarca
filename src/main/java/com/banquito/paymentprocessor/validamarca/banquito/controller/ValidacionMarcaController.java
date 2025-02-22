package com.banquito.paymentprocessor.validamarca.banquito.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banquito.paymentprocessor.validamarca.banquito.client.dto.MarcaResponse;
import com.banquito.paymentprocessor.validamarca.banquito.controller.dto.ValidacionMarcaRequestDTO;
import com.banquito.paymentprocessor.validamarca.banquito.controller.dto.ValidacionMarcaResponseDTO;
import com.banquito.paymentprocessor.validamarca.banquito.service.ValidacionMarcaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/validacion")
@Tag(name = "Validación de Marca", description = "API para el proceso de validación de tarjetas con su marca correspondiente")
@Slf4j
public class ValidacionMarcaController {

    private final ValidacionMarcaService service;

    public ValidacionMarcaController(ValidacionMarcaService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Procesa la validación de una tarjeta con su marca", 
              description = "Realiza el proceso de validación de una tarjeta con su marca correspondiente y obtiene el SWIFT del banco")
    public ResponseEntity<ValidacionMarcaResponseDTO> validarTarjeta(
            @Valid @RequestBody ValidacionMarcaRequestDTO request) {
        log.info("Recibida solicitud de validación para marca: {}", request.getMarca());
        
        MarcaResponse marcaResponse = service.validarTarjeta(
            request.getNumeroTarjeta(),
            request.getMarca(),
            request.getCvv(),
            request.getFechaCaducidad()
        );

        ValidacionMarcaResponseDTO response = new ValidacionMarcaResponseDTO();
        response.setTarjetaValida(marcaResponse.getTarjetaValida());
        response.setSwiftBanco(marcaResponse.getSwiftBanco());
        response.setMensaje(marcaResponse.getMensaje());

        return ResponseEntity.ok(response);
    }
} 