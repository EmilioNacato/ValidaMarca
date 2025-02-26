package com.banquito.paymentprocessor.validamarca.banquito.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banquito.paymentprocessor.validamarca.banquito.client.dto.MarcaResponse;
import com.banquito.paymentprocessor.validamarca.banquito.dto.ValidacionMarcaRequestDTO;
import com.banquito.paymentprocessor.validamarca.banquito.dto.ValidacionMarcaResponseDTO;
import com.banquito.paymentprocessor.validamarca.banquito.service.ValidacionMarcaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Slf4j
@RestController
@RequestMapping("/api/v1/marca")
@Tag(name = "Validación de Marca", description = "API para validar tarjetas con su marca")
public class ValidacionMarcaController {
    
    private final ValidacionMarcaService service;

    public ValidacionMarcaController(ValidacionMarcaService service) {
        this.service = service;
    }

    @PostMapping("/validar")
    @Operation(summary = "Valida una tarjeta con su marca", 
              description = "Valida que la tarjeta sea válida y pertenezca a la marca indicada")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Validación completada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de la tarjeta inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<ValidacionMarcaResponseDTO> validarMarca(
            @Valid @RequestBody ValidacionMarcaRequestDTO request) {
        log.info("Recibida solicitud de validación para marca: {}", request.getMarca());
        
        ValidacionMarcaResponseDTO response = service.validarMarca(request);
        
        log.info("Validación completada para marca: {}", request.getMarca());
        
        return ResponseEntity.ok(response);
    }
} 