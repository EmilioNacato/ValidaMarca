package com.banquito.paymentprocessor.validamarca.banquito.controller;

import com.banquito.paymentprocessor.validamarca.banquito.controller.dto.ValidacionMarcaRequestDTO;
import com.banquito.paymentprocessor.validamarca.banquito.controller.dto.ValidacionMarcaResponseDTO;
import com.banquito.paymentprocessor.validamarca.banquito.service.ValidacionMarcaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/validacion-marca")
@Tag(name = "Validación de Marca", description = "API para el proceso de validación de tarjetas con su marca correspondiente")
@Slf4j
public class ValidacionMarcaController {

    private final ValidacionMarcaService service;

    public ValidacionMarcaController(ValidacionMarcaService service) {
        this.service = service;
    }

    @PostMapping("/validar")
    @Operation(summary = "Procesa la validación de una tarjeta con su marca", 
              description = "Realiza el proceso de validación de una tarjeta con su marca correspondiente y obtiene el SWIFT del banco")
    public ResponseEntity<ValidacionMarcaResponseDTO> validarMarca(
            @RequestBody ValidacionMarcaRequestDTO request) {
        log.info("Iniciando validación de marca para tarjeta: {}", request.getNumeroTarjeta());
        ValidacionMarcaResponseDTO response = service.validarMarca(request);
        return ResponseEntity.ok(response);
    }
} 