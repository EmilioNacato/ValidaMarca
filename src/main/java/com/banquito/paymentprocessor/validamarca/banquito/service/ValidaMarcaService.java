package com.banquito.paymentprocessor.validamarca.banquito.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
@Slf4j
public class ValidaMarcaService {

    @Value("${marcas.aceptadas}")
    private List<String> marcasAceptadas;

    public ValidacionMarcaResponseDTO validarMarca(ValidacionMarcaRequestDTO request) {
        ValidacionMarcaResponseDTO response = new ValidacionMarcaResponseDTO();
        
        try {
            String marca = identificarMarca(request.getNumeroTarjeta());
            
            if (marcasAceptadas.contains(marca)) {
                response.setValida(true);
                response.setMarca(marca);
                response.setSwiftBanco(obtenerSwiftBanco(request.getNumeroTarjeta()));
            } else {
                response.setValida(false);
                response.setMensaje("Marca de tarjeta no aceptada");
            }
            
        } catch (Exception e) {
            log.error("Error al validar marca: {}", e.getMessage());
            response.setValida(false);
            response.setMensaje("Error en validación de marca");
        }
        
        return response;
    }

    private String identificarMarca(String numeroTarjeta) {
        String bin = numeroTarjeta.substring(0, 6);
        // Lógica para identificar marca según BIN
        return "VISA"; // Ejemplo simplificado
    }

    private String obtenerSwiftBanco(String numeroTarjeta) {
        // Lógica para obtener SWIFT según BIN
        return "BANKSWIFT"; // Ejemplo simplificado
    }
} 