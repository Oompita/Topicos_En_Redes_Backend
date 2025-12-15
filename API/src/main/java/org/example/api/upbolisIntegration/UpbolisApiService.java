package org.example.api.upbolisIntegration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

/**
 * Servicio para comunicarse con la API externa de UPBolis (marketplace/pasarela de pagos)
 * Maneja autenticación JWT y registro de cursos con precios
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UpbolisApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${upbolis.api.base-url}")
    private String upbolisApiBaseUrl;

    @Value("${upbolis.api.username}")
    private String upbolisUsername;

    @Value("${upbolis.api.password}")
    private String upbolisPassword;

    private String cachedToken = null;

    /**
     * Obtiene un token JWT de la API de UPBolis
     * Cachea el token para reutilizarlo en múltiples llamadas
     */
    private String obtenerToken() {
        try {
            log.info("Obteniendo token JWT de UPBolis API...");

            // Preparar request de login
            UpbolisLoginRequest loginRequest = new UpbolisLoginRequest(upbolisUsername, upbolisPassword);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<UpbolisLoginRequest> request = new HttpEntity<>(loginRequest, headers);

            // Llamar al endpoint de login
            ResponseEntity<UpbolisLoginResponse> response = restTemplate.postForEntity(
                    upbolisApiBaseUrl + "/auth/login",
                    request,
                    UpbolisLoginResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                cachedToken = response.getBody().getToken();
                log.info("Token JWT de UPBolis obtenido exitosamente");
                return cachedToken;
            } else {
                log.error("Error al obtener token de UPBolis: Status code {}", response.getStatusCode());
                throw new RuntimeException("Error al obtener token de UPBolis API");
            }

        } catch (Exception e) {
            log.error("Error al comunicarse con UPBolis API para login: {}", e.getMessage());
            throw new RuntimeException("Error al autenticar con UPBolis API", e);
        }
    }

    /**
     * Registra o actualiza un curso en UPBolis
     * Envía ID, nombre, descripción y precio del curso
     */
    public UpbolisCursoResponse registrarCurso(Long cursoId, String nombre, String descripcion, Double precio) {
        try {
            log.info("Registrando curso en UPBolis - ID: {}, Nombre: {}, Precio: {}", cursoId, nombre, precio);

            // Obtener token JWT (usa caché si existe)
            if (cachedToken == null) {
                obtenerToken();
            }

            // Preparar request con la información del curso
            UpbolisCursoRequest cursoRequest = UpbolisCursoRequest.builder()
                    .cursoId(cursoId)
                    .nombre(nombre)
                    .descripcion(descripcion)
                    .precio(precio != null ? precio : 0.0) // 0.0 si es gratis
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(cachedToken);

            HttpEntity<UpbolisCursoRequest> request = new HttpEntity<>(cursoRequest, headers);

            // Enviar curso a UPBolis
            ResponseEntity<UpbolisCursoResponse> response = restTemplate.postForEntity(
                    upbolisApiBaseUrl + "/cursos/register", // Ajustar endpoint según la API real
                    request,
                    UpbolisCursoResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Curso registrado exitosamente en UPBolis: {}", response.getBody().getMessage());
                return response.getBody();
            } else if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                // Token expirado, renovar y reintentar
                log.warn("Token de UPBolis expirado, renovando...");
                cachedToken = null;
                obtenerToken();
                return registrarCurso(cursoId, nombre, descripcion, precio); // Retry recursivo
            } else {
                log.error("Error al registrar curso en UPBolis: Status code {}", response.getStatusCode());
                throw new RuntimeException("Error al comunicarse con UPBolis API");
            }

        } catch (Exception e) {
            log.error("Error al registrar curso en UPBolis: {}", e.getMessage());
            // No lanzamos excepción para no interrumpir el flujo principal
            return new UpbolisCursoResponse(
                    "Error: " + e.getMessage(),
                    cursoId,
                    null,
                    "error"
            );
        }
    }

    /**
     * Actualiza el precio de un curso en UPBolis
     */
    public UpbolisCursoResponse actualizarPrecioCurso(Long cursoId, Double nuevoPrecio) {
        try {
            log.info("Actualizando precio del curso {} en UPBolis: {}", cursoId, nuevoPrecio);

            if (cachedToken == null) {
                obtenerToken();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(cachedToken);

            // Crear request con nuevo precio
            UpbolisCursoRequest updateRequest = UpbolisCursoRequest.builder()
                    .cursoId(cursoId)
                    .precio(nuevoPrecio)
                    .build();

            HttpEntity<UpbolisCursoRequest> request = new HttpEntity<>(updateRequest, headers);

            // Actualizar precio
            ResponseEntity<UpbolisCursoResponse> response = restTemplate.exchange(
                    upbolisApiBaseUrl + "/cursos/" + cursoId + "/precio",
                    HttpMethod.PUT,
                    request,
                    UpbolisCursoResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Precio actualizado exitosamente en UPBolis");
                return response.getBody();
            } else if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                cachedToken = null;
                obtenerToken();
                return actualizarPrecioCurso(cursoId, nuevoPrecio);
            } else {
                throw new RuntimeException("Error al actualizar precio en UPBolis");
            }

        } catch (Exception e) {
            log.error("Error al actualizar precio en UPBolis: {}", e.getMessage());
            return new UpbolisCursoResponse(
                    "Error: " + e.getMessage(),
                    cursoId,
                    null,
                    "error"
            );
        }
    }

    /**
     * Elimina un curso de UPBolis
     */
    public boolean eliminarCurso(Long cursoId) {
        try {
            log.info("Eliminando curso {} de UPBolis", cursoId);

            if (cachedToken == null) {
                obtenerToken();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(cachedToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Void> response = restTemplate.exchange(
                    upbolisApiBaseUrl + "/cursos/" + cursoId,
                    HttpMethod.DELETE,
                    request,
                    Void.class
            );

            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.NO_CONTENT) {
                log.info("Curso eliminado exitosamente de UPBolis");
                return true;
            } else if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                cachedToken = null;
                obtenerToken();
                return eliminarCurso(cursoId);
            }

            return false;

        } catch (Exception e) {
            log.error("Error al eliminar curso de UPBolis: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Método de prueba para verificar conectividad con la API de UPBolis
     */
    public boolean verificarConectividad() {
        try {
            obtenerToken();
            return true;
        } catch (Exception e) {
            log.error("Error al verificar conectividad con UPBolis API: {}", e.getMessage());
            return false;
        }
    }
}