package com.banquito.paymentprocessor.validamarca.banquito.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MarcaRequest {
    private String numeroTarjeta;
    private String marca;
    private String cvv;
    private String fechaCaducidad;
} 