package com.banquito.paymentprocessor.validamarca.banquito.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.banquito.paymentprocessor.validamarca.banquito.client.dto.MarcaResponse;
import com.banquito.paymentprocessor.validamarca.banquito.controller.dto.ValidacionMarcaRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
    "app.marca-externa.url=http://localhost:${wiremock.server.port}"
})
public class MarcaClientTest {

    @Autowired
    private MarcaClient marcaClient;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private ValidacionMarcaRequestDTO requestVISA;
    private MarcaResponse responseExitosa;
    private MarcaResponse responseFallida;
    
    @BeforeEach
    void setUp() throws Exception {
        requestVISA = new ValidacionMarcaRequestDTO();
        requestVISA.setNumeroTarjeta("4111111111111111");
        requestVISA.setMarca("VISA");
        requestVISA.setCvv("123");
        requestVISA.setFechaCaducidad("12/25");
        
        responseExitosa = new MarcaResponse();
        responseExitosa.setTarjetaValida(true);
        responseExitosa.setSwiftBanco("BQTOECEC");
        responseExitosa.setMensaje("Tarjeta válida");
        
        responseFallida = new MarcaResponse();
        responseFallida.setTarjetaValida(false);
        responseFallida.setMensaje("Tarjeta inválida");
        
        // Limpiar stubs previos
        reset();
    }
    
    @Test
    void validarTarjeta_respuestaExitosa() throws Exception {
        // Configurar stub de WireMock
        stubFor(post(urlEqualTo("/api/v1/validate"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(responseExitosa))));
        
        // Ejecutar cliente
        MarcaResponse response = marcaClient.validarTarjeta(requestVISA);
        
        // Verificar respuesta
        assertNotNull(response);
        assertTrue(response.getTarjetaValida());
        assertEquals("BQTOECEC", response.getSwiftBanco());
        assertEquals("Tarjeta válida", response.getMensaje());
        
        // Verificar que se hizo la llamada correcta
        verify(postRequestedFor(urlEqualTo("/api/v1/validate"))
                .withHeader("Content-Type", containing("application/json")));
    }
    
    @Test
    void validarTarjeta_respuestaFallida() throws Exception {
        // Configurar stub de WireMock
        stubFor(post(urlEqualTo("/api/v1/validate"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(responseFallida))));
        
        // Modificar datos de la tarjeta para que falle
        requestVISA.setCvv("12"); // CVV inválido
        
        // Ejecutar cliente
        MarcaResponse response = marcaClient.validarTarjeta(requestVISA);
        
        // Verificar respuesta
        assertNotNull(response);
        assertEquals(false, response.getTarjetaValida());
        assertEquals("Tarjeta inválida", response.getMensaje());
        
        // Verificar que se hizo la llamada correcta
        verify(postRequestedFor(urlEqualTo("/api/v1/validate"))
                .withHeader("Content-Type", containing("application/json")));
    }
    
    @Test
    void validarTarjeta_errorServidor() throws Exception {
        // Configurar stub de WireMock para error de servidor
        stubFor(post(urlEqualTo("/api/v1/validate"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("{\"error\":\"Error interno del servidor\"}")));
        
        try {
            // Ejecutar cliente - debería lanzar excepción
            marcaClient.validarTarjeta(requestVISA);
        } catch (Exception e) {
            // Verificar que se capturó la excepción
            assertTrue(e.getMessage().contains("500") || e.getMessage().contains("Error"));
        }
        
        // Verificar que se hizo la llamada correcta
        verify(postRequestedFor(urlEqualTo("/api/v1/validate")));
    }
} 