package com.banquito.paymentprocessor.validamarca.banquito.service;

import org.springframework.stereotype.Service;

import com.banquito.paymentprocessor.validamarca.banquito.client.MarcaClient;
import com.banquito.paymentprocessor.validamarca.banquito.client.dto.MarcaRequest;
import com.banquito.paymentprocessor.validamarca.banquito.client.dto.MarcaResponse;
import com.banquito.paymentprocessor.validamarca.banquito.exception.ValidacionMarcaException;
import com.banquito.paymentprocessor.validamarca.banquito.controller.dto.ValidacionMarcaRequestDTO;
import com.banquito.paymentprocessor.validamarca.banquito.controller.dto.ValidacionMarcaResponseDTO;


import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ValidacionMarcaService {

    private final MarcaClient marcaClient;

    public ValidacionMarcaService(MarcaClient marcaClient) {
        this.marcaClient = marcaClient;
    }

    public MarcaResponse validarTarjeta(String numeroTarjeta, String marca, String cvv, String fechaCaducidad) {
        log.info("Iniciando proceso de validación con marca: {}", marca);
        try {
            MarcaRequest request = new MarcaRequest();
            request.setNumeroTarjeta(numeroTarjeta);
            request.setMarca(marca);
            request.setCvv(cvv);
            request.setFechaCaducidad(fechaCaducidad);

            MarcaResponse response = marcaClient.validarTarjeta(request);
            log.info("Respuesta recibida del servicio de marca. Tarjeta válida: {}", response.getTarjetaValida());
            
            return response;
        } catch (Exception e) {
            log.error("Error en el proceso de validación con marca: {}", e.getMessage());
            throw new ValidacionMarcaException("Error en el proceso de validación con marca: " + e.getMessage());
        }
    }
} 