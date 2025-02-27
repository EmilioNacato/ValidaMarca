package com.banquito.paymentprocessor.validamarca.banquito.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.banquito.paymentprocessor.validamarca.banquito.dto.ValidacionMarcaRequestDTO;
import com.banquito.paymentprocessor.validamarca.banquito.dto.ValidacionMarcaResponseDTO;
import com.banquito.paymentprocessor.validamarca.banquito.service.ValidacionMarcaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/marca")
@Tag(name = "Validación de Marca", 
     description = "API para la validación y verificación de tarjetas según su marca emisora")
public class ValidacionMarcaController {
    
    private final ValidacionMarcaService service;

    public ValidacionMarcaController(ValidacionMarcaService service) {
        this.service = service;
    }

    @PostMapping("/validar")
    @Operation(
        summary = "Valida una tarjeta con su marca", 
        description = "Valida que una tarjeta sea válida y pertenezca a la marca indicada. " +
                     "Realiza verificaciones del BIN, longitud, algoritmo de Luhn y otros " +
                     "criterios específicos de la marca de la tarjeta."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Validación completada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ValidacionMarcaResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de la tarjeta inválidos o incompletos",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "422",
            description = "La tarjeta no corresponde a la marca indicada",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<ValidacionMarcaResponseDTO> validarMarca(
            @Parameter(
                description = "Datos de la tarjeta a validar", 
                required = true,
                schema = @Schema(implementation = ValidacionMarcaRequestDTO.class)
            )
            @Valid @RequestBody ValidacionMarcaRequestDTO request) {
        
        log.info("Recibida solicitud de validación para marca: {}", request.getMarca());
        
        ValidacionMarcaResponseDTO response = service.validarMarca(request);
        
        log.info("Validación completada para marca: {}. Es válida: {}", 
            request.getMarca(), response.isTarjetaValida());
        
        return ResponseEntity.ok(response);
    }
} 