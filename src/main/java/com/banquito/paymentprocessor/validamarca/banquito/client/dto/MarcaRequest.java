package com.banquito.paymentprocessor.validamarca.banquito.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MarcaRequest {
    private String cardNumber;
    private String brand;
    private String cvv;
    private String expirationDate;
} 