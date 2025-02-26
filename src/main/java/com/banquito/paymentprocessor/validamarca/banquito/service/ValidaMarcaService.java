package com.banquito.paymentprocessor.validamarca.banquito.service;

import com.banquito.paymentprocessor.validamarca.banquito.dto.ValidacionMarcaRequestDTO;
import com.banquito.paymentprocessor.validamarca.banquito.dto.ValidacionMarcaResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class ValidaMarcaService {

    private List<String> marcasAceptadas;

    public ValidacionMarcaResponseDTO validarMarca(ValidacionMarcaRequestDTO request) {
        ValidacionMarcaResponseDTO response = new ValidacionMarcaResponseDTO();
        
        try {
            String marca = identificarMarca(request.getNumeroTarjeta());
            
            if (marca != null) {
                response.setTarjetaValida(true);
                response.setMarca(marca);
                response.setSwiftBanco(obtenerSwiftBanco(marca));
                response.setMensaje("Tarjeta válida");
            } else {
                response.setTarjetaValida(false);
                response.setMensaje("Marca de tarjeta no soportada");
            }
            
        } catch (Exception e) {
            log.error("Error al validar marca: {}", e.getMessage());
            response.setTarjetaValida(false);
            response.setMensaje("Error en validación de marca");
        }
        
        return response;
    }

    private String identificarMarca(String numeroTarjeta) {
        if (numeroTarjeta.startsWith("4")) {
            return "VISA";
        } else if (numeroTarjeta.startsWith("5")) {
            return "MASTERCARD";
        } else if (numeroTarjeta.startsWith("34") || numeroTarjeta.startsWith("37")) {
            return "AMEX";
        }
        return null;
    }

    private String obtenerSwiftBanco(String marca) {
        // Lógica simplificada para obtener el SWIFT del banco según la marca
        switch (marca) {
            case "VISA":
                return "BQTOECEC";
            case "MASTERCARD":
                return "PICHECEC";
            case "AMEX":
                return "GUAYECEC";
            default:
                return null;
        }
    }
} 