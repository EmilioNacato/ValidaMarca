package com.banquito.paymentprocessor.validamarca.banquito.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.banquito.paymentprocessor.validamarca.banquito.dto.ValidacionMarcaRequestDTO;
import com.banquito.paymentprocessor.validamarca.banquito.dto.ValidacionMarcaResponseDTO;
import com.banquito.paymentprocessor.validamarca.banquito.service.ValidacionMarcaService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ValidacionMarcaController.class)
public class ValidacionMarcaControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ValidacionMarcaService validacionMarcaService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private ValidacionMarcaRequestDTO requestVISA;
    private ValidacionMarcaRequestDTO requestMastercard;
    private ValidacionMarcaRequestDTO requestInvalida;
    private ValidacionMarcaResponseDTO responseExitosa;
    private ValidacionMarcaResponseDTO responseFallida;
    
    @BeforeEach
    void setUp() {
        requestVISA = new ValidacionMarcaRequestDTO();
        requestVISA.setNumeroTarjeta("4111111111111111");
        requestVISA.setMarca("VISA");
        requestVISA.setCvv("123");
        requestVISA.setFechaCaducidad("12/25");
        
        requestMastercard = new ValidacionMarcaRequestDTO();
        requestMastercard.setNumeroTarjeta("5111111111111118");
        requestMastercard.setMarca("MASTERCARD");
        requestMastercard.setCvv("123");
        requestMastercard.setFechaCaducidad("12/25");
        
        requestInvalida = new ValidacionMarcaRequestDTO();
        requestInvalida.setNumeroTarjeta("1234567890123456");
        requestInvalida.setMarca("DESCONOCIDA");
        requestInvalida.setCvv("123");
        requestInvalida.setFechaCaducidad("12/20");
        
        responseExitosa = new ValidacionMarcaResponseDTO();
        responseExitosa.setTarjetaValida(true);
        responseExitosa.setMarca("VISA");
        responseExitosa.setSwiftBanco("BQTOECEC");
        responseExitosa.setMensaje("Tarjeta válida");
        
        responseFallida = new ValidacionMarcaResponseDTO();
        responseFallida.setTarjetaValida(false);
        responseFallida.setMensaje("Tarjeta inválida");
    }
    
    @Test
    void validarMarca_tarjetaVISAValida_retornaRespuestaExitosa() throws Exception {
        when(validacionMarcaService.validarMarca(any(ValidacionMarcaRequestDTO.class)))
            .thenReturn(responseExitosa);
            
        mockMvc.perform(post("/api/v1/marca/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestVISA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tarjetaValida").value(true))
                .andExpect(jsonPath("$.marca").value("VISA"))
                .andExpect(jsonPath("$.swiftBanco").value("BQTOECEC"));
    }
    
    @Test
    void validarMarca_tarjetaMastercardValida_retornaRespuestaExitosa() throws Exception {
        ValidacionMarcaResponseDTO responseMastercard = new ValidacionMarcaResponseDTO();
        responseMastercard.setTarjetaValida(true);
        responseMastercard.setMarca("MASTERCARD");
        responseMastercard.setSwiftBanco("BQTOECMC");
        responseMastercard.setMensaje("Tarjeta válida");
        
        when(validacionMarcaService.validarMarca(any(ValidacionMarcaRequestDTO.class)))
            .thenReturn(responseMastercard);
            
        mockMvc.perform(post("/api/v1/marca/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestMastercard)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tarjetaValida").value(true))
                .andExpect(jsonPath("$.marca").value("MASTERCARD"))
                .andExpect(jsonPath("$.swiftBanco").value("BQTOECMC"));
    }
    
    @Test
    void validarMarca_tarjetaInvalida_retornaRespuestaFallida() throws Exception {
        when(validacionMarcaService.validarMarca(any(ValidacionMarcaRequestDTO.class)))
            .thenReturn(responseFallida);
            
        mockMvc.perform(post("/api/v1/marca/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalida)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tarjetaValida").value(false))
                .andExpect(jsonPath("$.mensaje").value("Tarjeta inválida"));
    }
    
    @Test
    void validarMarca_datosFaltantes_retornaBadRequest() throws Exception {
        ValidacionMarcaRequestDTO requestIncompleta = new ValidacionMarcaRequestDTO();
        requestIncompleta.setNumeroTarjeta("4111111111111111");
            
        mockMvc.perform(post("/api/v1/marca/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestIncompleta)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void validarMarca_errorInterno_retornaRespuestaConError() throws Exception {
        ValidacionMarcaResponseDTO responseError = new ValidacionMarcaResponseDTO();
        responseError.setTarjetaValida(false);
        responseError.setMensaje("Error en validación: Error interno");
        
        when(validacionMarcaService.validarMarca(any(ValidacionMarcaRequestDTO.class)))
            .thenReturn(responseError);
            
        mockMvc.perform(post("/api/v1/marca/validar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestVISA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tarjetaValida").value(false))
                .andExpect(jsonPath("$.mensaje").value("Error en validación: Error interno"));
    }
} 