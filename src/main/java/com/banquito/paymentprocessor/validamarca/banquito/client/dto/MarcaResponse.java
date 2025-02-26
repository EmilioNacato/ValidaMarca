package com.banquito.paymentprocessor.validamarca.banquito.client.dto;

import lombok.Data;

@Data
public class MarcaResponse {
    private boolean tarjetaValida;
    private String marca;
    private String swiftBanco;
    private String mensaje;
} 