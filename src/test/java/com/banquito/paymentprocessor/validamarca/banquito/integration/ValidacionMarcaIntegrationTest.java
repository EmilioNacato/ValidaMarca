package com.banquito.paymentprocessor.validamarca.banquito.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.banquito.paymentprocessor.validamarca.banquito.dto.ValidacionMarcaRequestDTO;
import com.banquito.paymentprocessor.validamarca.banquito.dto.ValidacionMarcaResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ValidacionMarcaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private RedisTemplate<String, String> redisTemplate;
    
    @MockBean
    private ValueOperations<String, String> valueOperations;
    
    private ValidacionMarcaRequestDTO requestVISA;
    private ValidacionMarcaRequestDTO requestMastercard;
    private ValidacionMarcaRequestDTO requestAMEX;
    private ValidacionMarcaRequestDTO requestInvalida;
    
    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        
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
    void validarMarca_tarjetaVISAValida_retornaRespuestaExitosa() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/marca/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestVISA)))
                .andExpect(status().isOk())
                .andReturn();
        
        ValidacionMarcaResponseDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                ValidacionMarcaResponseDTO.class
        );
        
        assertNotNull(response);
        assertTrue(response.isTarjetaValida());
        assertEquals("VISA", response.getMarca());
        assertEquals("BQTOECEC", response.getSwiftBanco());
        assertEquals("Tarjeta válida", response.getMensaje());
    }
    
    @Test
    void validarMarca_tarjetaMastercardValida_retornaRespuestaExitosa() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/marca/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestMastercard)))
                .andExpect(status().isOk())
                .andReturn();
        
        ValidacionMarcaResponseDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                ValidacionMarcaResponseDTO.class
        );
        
        assertNotNull(response);
        assertTrue(response.isTarjetaValida());
        assertEquals("MASTERCARD", response.getMarca());
        assertEquals("BQTOECMC", response.getSwiftBanco());
        assertEquals("Tarjeta válida", response.getMensaje());
    }
    
    @Test
    void validarMarca_tarjetaAMEXValida_retornaRespuestaExitosa() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/marca/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestAMEX)))
                .andExpect(status().isOk())
                .andReturn();
        
        ValidacionMarcaResponseDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                ValidacionMarcaResponseDTO.class
        );
        
        assertNotNull(response);
        assertTrue(response.isTarjetaValida());
        assertEquals("AMEX", response.getMarca());
        assertEquals("BQTOECAX", response.getSwiftBanco());
        assertEquals("Tarjeta válida", response.getMensaje());
    }
    
    @Test
    void validarMarca_tarjetaDesconocida_retornaRespuestaInvalida() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/marca/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalida)))
                .andExpect(status().isOk())
                .andReturn();
        
        ValidacionMarcaResponseDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                ValidacionMarcaResponseDTO.class
        );
        
        assertNotNull(response);
        assertFalse(response.isTarjetaValida());
        assertEquals("Marca de tarjeta no soportada: DESCONOCIDA", response.getMensaje());
    }
    
    @Test
    void validarMarca_marcaNoCoincideConBIN_retornaRespuestaInvalida() throws Exception {
        ValidacionMarcaRequestDTO requestMarcaIncorrecta = new ValidacionMarcaRequestDTO();
        requestMarcaIncorrecta.setNumeroTarjeta("4111111111111111"); // BIN de VISA
        requestMarcaIncorrecta.setMarca("MASTERCARD"); // Pero dice que es Mastercard
        requestMarcaIncorrecta.setCvv("123");
        requestMarcaIncorrecta.setFechaCaducidad("12/25");
        
        MvcResult result = mockMvc.perform(post("/api/v1/marca/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestMarcaIncorrecta)))
                .andExpect(status().isOk())
                .andReturn();
        
        ValidacionMarcaResponseDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                ValidacionMarcaResponseDTO.class
        );
        
        assertNotNull(response);
        assertFalse(response.isTarjetaValida());
        assertEquals("Marca declarada no coincide con BIN de tarjeta", response.getMensaje());
    }
    
    @Test
    void validarMarca_tarjetaFechaExpirada_retornaRespuestaInvalida() throws Exception {
        ValidacionMarcaRequestDTO requestExpirada = new ValidacionMarcaRequestDTO();
        requestExpirada.setNumeroTarjeta("4111111111111111");
        requestExpirada.setMarca("VISA");
        requestExpirada.setCvv("123");
        requestExpirada.setFechaCaducidad("01/20"); // Fecha expirada
        
        MvcResult result = mockMvc.perform(post("/api/v1/marca/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestExpirada)))
                .andExpect(status().isOk())
                .andReturn();
        
        ValidacionMarcaResponseDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                ValidacionMarcaResponseDTO.class
        );
        
        assertNotNull(response);
        assertFalse(response.isTarjetaValida());
        assertEquals("Fecha de caducidad inválida o expirada", response.getMensaje());
    }
    
    @Test
    void validarMarca_cvvInvalido_retornaRespuestaInvalida() throws Exception {
        ValidacionMarcaRequestDTO requestCvvInvalido = new ValidacionMarcaRequestDTO();
        requestCvvInvalido.setNumeroTarjeta("4111111111111111");
        requestCvvInvalido.setMarca("VISA");
        requestCvvInvalido.setCvv("12"); // CVV inválido para VISA (debe ser 3 dígitos)
        requestCvvInvalido.setFechaCaducidad("12/25");
        
        MvcResult result = mockMvc.perform(post("/api/v1/marca/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestCvvInvalido)))
                .andExpect(status().isOk())
                .andReturn();
        
        ValidacionMarcaResponseDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                ValidacionMarcaResponseDTO.class
        );
        
        assertNotNull(response);
        assertFalse(response.isTarjetaValida());
        assertEquals("CVV inválido", response.getMensaje());
    }
    
    @Test
    void validarMarca_cvvInvalidoParaAMEX_retornaRespuestaInvalida() throws Exception {
        ValidacionMarcaRequestDTO requestCvvInvalido = new ValidacionMarcaRequestDTO();
        requestCvvInvalido.setNumeroTarjeta("371111111111114");
        requestCvvInvalido.setMarca("AMEX");
        requestCvvInvalido.setCvv("123"); // CVV inválido para AMEX (debe ser 4 dígitos)
        requestCvvInvalido.setFechaCaducidad("12/25");
        
        MvcResult result = mockMvc.perform(post("/api/v1/marca/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestCvvInvalido)))
                .andExpect(status().isOk())
                .andReturn();
        
        ValidacionMarcaResponseDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                ValidacionMarcaResponseDTO.class
        );
        
        assertNotNull(response);
        assertFalse(response.isTarjetaValida());
        assertEquals("CVV inválido", response.getMensaje());
    }
    
    @Test
    void validarMarca_numeroTarjetaAlgoritmoLuhnInvalido_retornaRespuestaInvalida() throws Exception {
        ValidacionMarcaRequestDTO requestLuhnInvalido = new ValidacionMarcaRequestDTO();
        requestLuhnInvalido.setNumeroTarjeta("4111111111111112"); // Número que no cumple Luhn
        requestLuhnInvalido.setMarca("VISA");
        requestLuhnInvalido.setCvv("123");
        requestLuhnInvalido.setFechaCaducidad("12/25");
        
        MvcResult result = mockMvc.perform(post("/api/v1/marca/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestLuhnInvalido)))
                .andExpect(status().isOk())
                .andReturn();
        
        ValidacionMarcaResponseDTO response = objectMapper.readValue(
                result.getResponse().getContentAsString(), 
                ValidacionMarcaResponseDTO.class
        );
        
        assertNotNull(response);
        assertFalse(response.isTarjetaValida());
        assertEquals("Número de tarjeta inválido", response.getMensaje());
    }
    
    @Test
    void validarMarca_datosInvalidos_retornaBadRequest() throws Exception {
        ValidacionMarcaRequestDTO requestInvalida = new ValidacionMarcaRequestDTO();
        // Falta el número de tarjeta y otros campos obligatorios
        
        mockMvc.perform(post("/api/v1/marca/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalida)))
                .andExpect(status().isBadRequest());
    }
} 