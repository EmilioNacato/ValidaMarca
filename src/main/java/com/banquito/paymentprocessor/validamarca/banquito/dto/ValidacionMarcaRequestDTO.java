package com.banquito.paymentprocessor.validamarca.banquito.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
public class ValidacionMarcaRequestDTO {
    @NotBlank
    @Pattern(regexp = "^[0-9]{16}$")
    private String numeroTarjeta;
    
    @NotBlank
    private String marca;
    
    @NotBlank
    @Pattern(regexp = "^[0-9]{3,4}$")
    private String cvv;
    
    @NotBlank
    @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$")
    private String fechaCaducidad;
} 