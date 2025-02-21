package com.banquito.paymentprocessor.validamarca.banquito.service;

import org.springframework.stereotype.Service;

import com.banquito.paymentprocessor.validamarca.banquito.client.MarcaClient;
import com.banquito.paymentprocessor.validamarca.banquito.client.dto.MarcaRequest;
import com.banquito.paymentprocessor.validamarca.banquito.client.dto.MarcaResponse;
import com.banquito.paymentprocessor.validamarca.banquito.exception.ValidacionMarcaException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ValidacionMarcaService {

    private final MarcaClient marcaClient;

    public ValidacionMarcaService(MarcaClient marcaClient) {
        this.marcaClient = marcaClient;
    }

    public MarcaResponse validarTarjeta(String numeroTarjeta, String marca, String cvv, String fechaCaducidad) {
        log.debug("Iniciando validación de tarjeta con marca: {}", marca);
        try {
            MarcaRequest request = new MarcaRequest();
            request.setCardNumber(numeroTarjeta);
            request.setBrand(marca);
            request.setCvv(cvv);
            request.setExpirationDate(fechaCaducidad);

            return marcaClient.validarTarjeta(request);
        } catch (Exception e) {
            log.error("Error al validar tarjeta con marca: {}", marca, e);
            throw new ValidacionMarcaException("Error al validar tarjeta con la marca", e);
        }
    }
} 