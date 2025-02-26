package com.banquito.paymentprocessor.validamarca.banquito.service;

import com.banquito.paymentprocessor.validamarca.banquito.client.MarcaClient;
import com.banquito.paymentprocessor.validamarca.banquito.client.dto.MarcaResponse;
import com.banquito.paymentprocessor.validamarca.banquito.exception.ValidacionMarcaException;
import com.banquito.paymentprocessor.validamarca.banquito.dto.ValidacionMarcaRequestDTO;
import com.banquito.paymentprocessor.validamarca.banquito.dto.ValidacionMarcaResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ValidacionMarcaService {

    @Value("${validacion.marca.maxIntentos:3}")
    private Integer maxIntentos;

    @Value("${validacion.marca.tiempoBloqueo:30}")
    private Integer tiempoBloqueo;

    private final MarcaClient marcaClient;
    private final RedisTemplate<String, String> redisTemplate;

    private static final Map<String, String> SWIFT_POR_MARCA = Map.of(
        "VISA", "BQTOECEC",
        "MASTERCARD", "BQTOECMC",
        "AMEX", "BQTOECAX"
    );

    private static final List<String> MARCAS_ACEPTADAS = List.of("VISA", "MASTERCARD", "AMEX");

    public ValidacionMarcaService(MarcaClient marcaClient, RedisTemplate<String, String> redisTemplate) {
        this.marcaClient = marcaClient;
        this.redisTemplate = redisTemplate;
    }

    public ValidacionMarcaResponseDTO validarMarca(ValidacionMarcaRequestDTO request) {
        log.info("Iniciando validación de marca para tarjeta: {}", request.getNumeroTarjeta());
        ValidacionMarcaResponseDTO response = new ValidacionMarcaResponseDTO();

        try {
            // 1. Validar si la tarjeta está bloqueada
            if (esTarjetaBloqueada(request.getNumeroTarjeta())) {
                return crearRespuestaInvalida("Tarjeta bloqueada por múltiples intentos fallidos");
            }

            // 2. Identificar marca por BIN
            String marca = identificarMarcaPorBIN(request.getNumeroTarjeta());
            if (!MARCAS_ACEPTADAS.contains(marca)) {
                return crearRespuestaInvalida("Marca de tarjeta no soportada: " + marca);
            }

            // 3. Validar que la marca coincida con la enviada
            if (!marca.equals(request.getMarca())) {
                return crearRespuestaInvalida("Marca declarada no coincide con BIN de tarjeta");
            }

            // 4. Validar algoritmo de Luhn
            if (!validarAlgoritmoLuhn(request.getNumeroTarjeta())) {
                incrementarIntentosFallidos(request.getNumeroTarjeta());
                return crearRespuestaInvalida("Número de tarjeta inválido");
            }

            // 5. Validar fecha de caducidad
            if (!validarFechaCaducidad(request.getFechaCaducidad())) {
                incrementarIntentosFallidos(request.getNumeroTarjeta());
                return crearRespuestaInvalida("Fecha de caducidad inválida o expirada");
            }

            // 6. Validar CVV
            if (!validarCVV(request.getCvv(), marca)) {
                incrementarIntentosFallidos(request.getNumeroTarjeta());
                return crearRespuestaInvalida("CVV inválido");
            }

            // Si todas las validaciones son exitosas
            limpiarIntentosFallidos(request.getNumeroTarjeta());
            response.setTarjetaValida(true);
            response.setMarca(marca);
            response.setSwiftBanco(SWIFT_POR_MARCA.get(marca));
            response.setMensaje("Tarjeta válida");

            return response;
        } catch (Exception e) {
            log.error("Error en validación de marca: {}", e.getMessage());
            response.setTarjetaValida(false);
            response.setMensaje("Error en validación: " + e.getMessage());
            return response;
        }
    }

    private boolean esTarjetaBloqueada(String numeroTarjeta) {
        String key = "bloqueo:tarjeta:" + numeroTarjeta;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private void incrementarIntentosFallidos(String numeroTarjeta) {
        String key = "intentos:tarjeta:" + numeroTarjeta;
        Long intentos = redisTemplate.opsForValue().increment(key);
        
        if (intentos != null && intentos >= maxIntentos) {
            String keyBloqueo = "bloqueo:tarjeta:" + numeroTarjeta;
            redisTemplate.opsForValue().set(keyBloqueo, "bloqueada");
            redisTemplate.expire(keyBloqueo, tiempoBloqueo, TimeUnit.MINUTES);
            log.warn("Tarjeta {} bloqueada por múltiples intentos fallidos", numeroTarjeta);
        }
    }

    private void limpiarIntentosFallidos(String numeroTarjeta) {
        String key = "intentos:tarjeta:" + numeroTarjeta;
        redisTemplate.delete(key);
    }

    private boolean validarAlgoritmoLuhn(String numeroTarjeta) {
        if (numeroTarjeta == null || numeroTarjeta.length() != 16) {
            return false;
        }

        int sum = 0;
        boolean alternate = false;
        
        for (int i = numeroTarjeta.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(numeroTarjeta.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        
        return (sum % 10 == 0);
    }

    private boolean validarFechaCaducidad(String fechaCaducidad) {
        try {
            String[] parts = fechaCaducidad.split("/");
            int mes = Integer.parseInt(parts[0]);
            int anio = Integer.parseInt(parts[1]) + 2000;
            
            LocalDate fechaTarjeta = LocalDate.of(anio, mes, 1)
                .plusMonths(1).minusDays(1);
            return fechaTarjeta.isAfter(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean validarCVV(String cvv, String marca) {
        if (cvv == null) return false;
        
        int longitudEsperada = marca.equals("AMEX") ? 4 : 3;
        return cvv.matches("\\d{" + longitudEsperada + "}");
    }

    private ValidacionMarcaResponseDTO crearRespuestaInvalida(String mensaje) {
        ValidacionMarcaResponseDTO response = new ValidacionMarcaResponseDTO();
        response.setTarjetaValida(false);
        response.setMensaje(mensaje);
        return response;
    }

    private String identificarMarcaPorBIN(String numeroTarjeta) {
        if (numeroTarjeta == null || numeroTarjeta.length() < 2) {
            return "DESCONOCIDA";
        }

        String bin = numeroTarjeta.substring(0, 2);
        switch (bin) {
            case "34":
            case "37":
                return "AMEX";
            case "51":
            case "52":
            case "53":
            case "54":
            case "55":
                return "MASTERCARD";
            case "40":
            case "41":
            case "42":
            case "43":
            case "44":
            case "45":
            case "46":
            case "47":
            case "48":
            case "49":
                return "VISA";
            default:
                return "DESCONOCIDA";
        }
    }
} 