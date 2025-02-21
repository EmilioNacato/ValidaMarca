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
@RequestMapping("/api/v1/validacion-marca")
@Tag(name = "Validación de Marca", description = "API para validar tarjetas con su marca correspondiente")
@Slf4j
public class ValidacionMarcaController {

    private final ValidacionMarcaService service;

    public ValidacionMarcaController(ValidacionMarcaService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Valida una tarjeta con su marca", 
              description = "Valida la autenticidad de una tarjeta con su marca correspondiente y retorna el SWIFT del banco adquirente")
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
        response.setEsValida(marcaResponse.getIsValid());
        response.setSwiftBancoAdquirente(marcaResponse.getAcquirerSwift());
        response.setMensajeError(marcaResponse.getErrorMessage());

        return ResponseEntity.ok(response);
    }
} 