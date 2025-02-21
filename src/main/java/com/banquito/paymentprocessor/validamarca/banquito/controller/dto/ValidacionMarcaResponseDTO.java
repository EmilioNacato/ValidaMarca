package com.banquito.paymentprocessor.validamarca.banquito.controller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ValidacionMarcaResponseDTO {
    private Boolean esValida;
    private String swiftBancoAdquirente;
    private String mensajeError;
} 