package com.banquito.paymentprocessor.validamarca.banquito.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ValidacionMarcaRequestDTO {
    private String numeroTarjeta;
    private BigDecimal monto;
    private String codigoUnico;
} 