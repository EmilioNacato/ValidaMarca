package com.banquito.paymentprocessor.validamarca.banquito.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//import com.banquito.paymentprocessor.validamarca.banquito.client.dto.MarcaRequest;
import com.banquito.paymentprocessor.validamarca.banquito.client.dto.MarcaResponse;
import com.banquito.paymentprocessor.validamarca.banquito.controller.dto.ValidacionMarcaRequestDTO;

@FeignClient(name = "marca-externa", url = "${app.marca-externa.url}")
public interface MarcaClient {
    
    @PostMapping("/api/v1/validate")
    MarcaResponse validarTarjeta(@RequestBody ValidacionMarcaRequestDTO request);
} 