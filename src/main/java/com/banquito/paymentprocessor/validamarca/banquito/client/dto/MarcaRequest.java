package com.banquito.paymentprocessor.validamarca.banquito.client.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MarcaRequest {
    private String numeroTarjeta;
    private BigDecimal monto;
    private String codigoUnico;
} 