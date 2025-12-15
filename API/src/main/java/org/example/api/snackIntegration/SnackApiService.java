package org.example.api.snackIntegration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

/**
 * Servicio para comunicarse con la API externa de Snack
 * Maneja autenticación JWT y envío de métricas de vistas
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SnackApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${snack.api.base-url}")
    private String snackApiBaseUrl;

    @Value("${snack.api.username}")
    private String snackUsername;

    @Value("${snack.api.password}")
    private String snackPassword;

    private String cachedToken = null;

    /**
     * Obtiene un token JWT de la API de Snack
     * Cachea el token para reutilizarlo en múltiples llamadas
     */
    private String obtenerToken() {
        try {
            log.info("Obteniendo token JWT de Snack API...");

            // Preparar request de login
            SnackLoginRequest loginRequest = new SnackLoginRequest(snackUsername, snackPassword);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<SnackLoginRequest> request = new HttpEntity<>(loginRequest, headers);

            // Llamar al endpoint de login
            ResponseEntity<SnackLoginResponse> response = restTemplate.postForEntity(
                    snackApiBaseUrl + "/auth/login",
                    request,
                    SnackLoginResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                cachedToken = response.getBody().getToken();
                log.info("Token JWT obtenido exitosamente");
                return cachedToken;
            } else {
                log.error("Error al obtener token: Status code {}", response.getStatusCode());
                throw new RuntimeException("Error al obtener token de Snack API");
            }

        } catch (Exception e) {
            log.error("Error al comunicarse con Snack API para login: {}", e.getMessage());
            throw new RuntimeException("Error al autenticar con Snack API", e);
        }
    }

    /**
     * Notifica a la API de Snack cuando un curso alcanza 10 vistas
     * Retorna el código de descuento proporcionado por Snack
     */
    public String notificarVistasYObtenerCodigo(Long cursoId, Long vistasActuales) {
        try {
            log.info("Notificando a Snack API - Curso ID: {}, Vistas: {}", cursoId, vistasActuales);

            // Obtener token JWT (usa caché si existe)
            if (cachedToken == null) {
                obtenerToken();
            }

            // Preparar request con las vistas
            SnackViewsRequest viewsRequest = new SnackViewsRequest(vistasActuales);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(cachedToken);

            HttpEntity<SnackViewsRequest> request = new HttpEntity<>(viewsRequest, headers);

            // Enviar notificación y recibir código
            ResponseEntity<SnackCodeResponse> response = restTemplate.postForEntity(
                    snackApiBaseUrl + "/views/notify",
                    request,
                    SnackCodeResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String codigo = response.getBody().getCode();
                log.info("Código de descuento recibido de Snack: {}", codigo);
                return codigo;
            } else if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                // Token expirado, renovar y reintentar
                log.warn("Token expirado, renovando...");
                cachedToken = null;
                obtenerToken();
                return notificarVistasYObtenerCodigo(cursoId, vistasActuales); // Retry recursivo
            } else {
                log.error("Error al notificar vistas: Status code {}", response.getStatusCode());
                throw new RuntimeException("Error al comunicarse con Snack API");
            }

        } catch (Exception e) {
            log.error("Error al comunicarse con Snack API: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Método de prueba para verificar conectividad con la API de Snack
     */
    public boolean verificarConectividad() {
        try {
            obtenerToken();
            return true;
        } catch (Exception e) {
            log.error("Error al verificar conectividad con Snack API: {}", e.getMessage());
            return false;
        }
    }
}