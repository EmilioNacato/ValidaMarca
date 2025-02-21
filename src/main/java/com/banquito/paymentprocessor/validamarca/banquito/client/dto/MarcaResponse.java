package com.banquito.paymentprocessor.validamarca.banquito.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MarcaResponse {
    private Boolean isValid;
    private String acquirerSwift;
    private String errorMessage;
} 