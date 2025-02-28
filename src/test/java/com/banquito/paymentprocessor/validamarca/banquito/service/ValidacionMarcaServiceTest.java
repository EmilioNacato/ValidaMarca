package com.banquito.paymentprocessor.validamarca.banquito.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import com.banquito.paymentprocessor.validamarca.banquito.client.MarcaClient;
import com.banquito.paymentprocessor.validamarca.banquito.client.dto.MarcaResponse;
import com.banquito.paymentprocessor.validamarca.banquito.dto.ValidacionMarcaRequestDTO;
import com.banquito.paymentprocessor.validamarca.banquito.dto.ValidacionMarcaResponseDTO;

@ExtendWith(MockitoExtension.class)
public class ValidacionMarcaServiceTest {

    @Mock
    private MarcaClient marcaClient;
    
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    
    @Mock
    private ValueOperations<String, String> valueOperations;
    
    @InjectMocks
    private ValidacionMarcaService validacionMarcaService;
    
    private ValidacionMarcaRequestDTO requestVISA;
    private ValidacionMarcaRequestDTO requestMastercard;
    private ValidacionMarcaRequestDTO requestAMEX;
    private ValidacionMarcaRequestDTO requestInvalida;
    
    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        // Configurar propiedades usando ReflectionTestUtils
        ReflectionTestUtils.setField(validacionMarcaService, "maxIntentos", 3);
        ReflectionTestUtils.setField(validacionMarcaService, "tiempoBloqueo", 30);
        
        // VISA válida
        requestVISA = new ValidacionMarcaRequestDTO();
        requestVISA.setNumeroTarjeta("4111111111111111");
        requestVISA.setMarca("VISA");
        requestVISA.setCvv("123");
        requestVISA.setFechaCaducidad("12/25");
        
        // Mastercard válida
        requestMastercard = new ValidacionMarcaRequestDTO();
        requestMastercard.setNumeroTarjeta("5111111111111118");
        requestMastercard.setMarca("MASTERCARD");
        requestMastercard.setCvv("123");
        requestMastercard.setFechaCaducidad("12/25");
        
        // AMEX válida
        requestAMEX = new ValidacionMarcaRequestDTO();
        requestAMEX.setNumeroTarjeta("371111111111114");
        requestAMEX.setMarca("AMEX");
        requestAMEX.setCvv("1234");
        requestAMEX.setFechaCaducidad("12/25");
        
        // Tarjeta inválida
        requestInvalida = new ValidacionMarcaRequestDTO();
        requestInvalida.setNumeroTarjeta("1234567890123456");
        requestInvalida.setMarca("DESCONOCIDA");
        requestInvalida.setCvv("123");
        requestInvalida.setFechaCaducidad("12/20");
    }
    
    @Test
    void validarMarca_tarjetaVISAValida_retornaRespuestaExitosa() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        
        ValidacionMarcaResponseDTO respuesta = validacionMarcaService.validarMarca(requestVISA);
        
        assertTrue(respuesta.isTarjetaValida());
        assertEquals("VISA", respuesta.getMarca());
        assertEquals("BQTOECEC", respuesta.getSwiftBanco());
        assertEquals("Tarjeta válida", respuesta.getMensaje());
    }
    
    @Test
    void validarMarca_tarjetaMastercardValida_retornaRespuestaExitosa() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        
        ValidacionMarcaResponseDTO respuesta = validacionMarcaService.validarMarca(requestMastercard);
        
        assertTrue(respuesta.isTarjetaValida());
        assertEquals("MASTERCARD", respuesta.getMarca());
        assertEquals("BQTOECMC", respuesta.getSwiftBanco());
        assertEquals("Tarjeta válida", respuesta.getMensaje());
    }
    
    @Test
    void validarMarca_tarjetaAMEXValida_retornaRespuestaExitosa() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        
        ValidacionMarcaResponseDTO respuesta = validacionMarcaService.validarMarca(requestAMEX);
        
        assertTrue(respuesta.isTarjetaValida());
        assertEquals("AMEX", respuesta.getMarca());
        assertEquals("BQTOECAX", respuesta.getSwiftBanco());
        assertEquals("Tarjeta válida", respuesta.getMensaje());
    }
    
    @Test
    void validarMarca_tarjetaNumeroBINInvalido_retornaRespuestaFallida() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        
        ValidacionMarcaResponseDTO respuesta = validacionMarcaService.validarMarca(requestInvalida);
        
        assertFalse(respuesta.isTarjetaValida());
        assertEquals("Marca de tarjeta no soportada: DESCONOCIDA", respuesta.getMensaje());
    }
    
    @Test
    void validarMarca_tarjetaMarcaNoCoincideConBIN_retornaRespuestaFallida() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        
        ValidacionMarcaRequestDTO request = new ValidacionMarcaRequestDTO();
        request.setNumeroTarjeta("4111111111111111"); // BIN de VISA
        request.setMarca("MASTERCARD"); // Pero dice que es Mastercard
        request.setCvv("123");
        request.setFechaCaducidad("12/25");
        
        ValidacionMarcaResponseDTO respuesta = validacionMarcaService.validarMarca(request);
        
        assertFalse(respuesta.isTarjetaValida());
        assertEquals("Marca declarada no coincide con BIN de tarjeta", respuesta.getMensaje());
    }
    
    @Test
    void validarMarca_tarjetaFechaExpiracion_retornaRespuestaFallida() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(valueOperations.increment(anyString())).thenReturn(1L);
        
        ValidacionMarcaRequestDTO request = new ValidacionMarcaRequestDTO();
        request.setNumeroTarjeta("4111111111111111");
        request.setMarca("VISA");
        request.setCvv("123");
        request.setFechaCaducidad("01/20"); // Fecha ya expirada
        
        ValidacionMarcaResponseDTO respuesta = validacionMarcaService.validarMarca(request);
        
        assertFalse(respuesta.isTarjetaValida());
        assertEquals("Fecha de caducidad inválida o expirada", respuesta.getMensaje());
        
        // Verificar que se incrementaron los intentos fallidos
        verify(valueOperations, times(1)).increment(anyString());
    }
    
    @Test
    void validarMarca_cvvInvalido_retornaRespuestaFallida() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(valueOperations.increment(anyString())).thenReturn(1L);
        
        ValidacionMarcaRequestDTO request = new ValidacionMarcaRequestDTO();
        request.setNumeroTarjeta("4111111111111111");
        request.setMarca("VISA");
        request.setCvv("12"); // CVV inválido para VISA (debe ser 3 dígitos)
        request.setFechaCaducidad("12/25");
        
        ValidacionMarcaResponseDTO respuesta = validacionMarcaService.validarMarca(request);
        
        assertFalse(respuesta.isTarjetaValida());
        assertEquals("CVV inválido", respuesta.getMensaje());
        
        // Verificar que se incrementaron los intentos fallidos
        verify(valueOperations, times(1)).increment(anyString());
    }
    
    @Test
    void validarMarca_cvvInvalidoParaAMEX_retornaRespuestaFallida() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(valueOperations.increment(anyString())).thenReturn(1L);
        
        ValidacionMarcaRequestDTO request = new ValidacionMarcaRequestDTO();
        request.setNumeroTarjeta("371111111111114");
        request.setMarca("AMEX");
        request.setCvv("123"); // CVV inválido para AMEX (debe ser 4 dígitos)
        request.setFechaCaducidad("12/25");
        
        ValidacionMarcaResponseDTO respuesta = validacionMarcaService.validarMarca(request);
        
        assertFalse(respuesta.isTarjetaValida());
        assertEquals("CVV inválido", respuesta.getMensaje());
        
        // Verificar que se incrementaron los intentos fallidos
        verify(valueOperations, times(1)).increment(anyString());
    }
    
    @Test
    void validarMarca_tarjetaBloqueada_retornaRespuestaFallida() {
        when(redisTemplate.hasKey(anyString())).thenReturn(true);
        
        ValidacionMarcaResponseDTO respuesta = validacionMarcaService.validarMarca(requestVISA);
        
        assertFalse(respuesta.isTarjetaValida());
        assertEquals("Tarjeta bloqueada por múltiples intentos fallidos", respuesta.getMensaje());
    }
    
    @Test
    void validarMarca_superaMaximosIntentos_bloqueaTarjeta() {
        when(redisTemplate.hasKey("bloqueo:tarjeta:4111111111111111")).thenReturn(false);
        when(valueOperations.increment("intentos:tarjeta:4111111111111111")).thenReturn(3L);
        
        ValidacionMarcaRequestDTO request = new ValidacionMarcaRequestDTO();
        request.setNumeroTarjeta("4111111111111111");
        request.setMarca("VISA");
        request.setCvv("12"); // CVV inválido para forzar incremento contador
        request.setFechaCaducidad("12/25");
        
        validacionMarcaService.validarMarca(request);
        
        // Verificar que se bloquea la tarjeta después de maxIntentos
        verify(valueOperations, times(1)).set(eq("bloqueo:tarjeta:4111111111111111"), eq("bloqueada"));
        verify(redisTemplate, times(1)).expire(
            eq("bloqueo:tarjeta:4111111111111111"), 
            eq(30L), 
            eq(TimeUnit.MINUTES)
        );
    }
    
    @Test
    void validarMarca_numeroTarjetaAlgoritmoLuhnInvalido_retornaRespuestaFallida() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(valueOperations.increment(anyString())).thenReturn(1L);
        
        ValidacionMarcaRequestDTO request = new ValidacionMarcaRequestDTO();
        request.setNumeroTarjeta("4111111111111112"); // Número que no cumple Luhn
        request.setMarca("VISA");
        request.setCvv("123");
        request.setFechaCaducidad("12/25");
        
        ValidacionMarcaResponseDTO respuesta = validacionMarcaService.validarMarca(request);
        
        assertFalse(respuesta.isTarjetaValida());
        assertEquals("Número de tarjeta inválido", respuesta.getMensaje());
        
        // Verificar que se incrementaron los intentos fallidos
        verify(valueOperations, times(1)).increment(anyString());
    }
    
    @Test
    void validarMarca_errorExcepcion_retornaRespuestaError() {
        when(redisTemplate.hasKey(anyString())).thenThrow(new RuntimeException("Error simulado"));
        
        ValidacionMarcaResponseDTO respuesta = validacionMarcaService.validarMarca(requestVISA);
        
        assertFalse(respuesta.isTarjetaValida());
        assertTrue(respuesta.getMensaje().contains("Error en validación"));
    }
} 