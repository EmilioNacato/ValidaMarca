package com.banquito.paymentprocessor.validamarca.banquito.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.banquito.paymentprocessor.validamarca.banquito.client.dto.MarcaResponse;
import com.banquito.paymentprocessor.validamarca.banquito.client.dto.MarcaRequest;

@FeignClient(name = "marca-service", url = "${app.marca-service.url}")
public interface MarcaClient {
    
    @PostMapping("/api/v1/validacion")
    MarcaResponse validarTarjeta(@RequestBody MarcaRequest request);
} 