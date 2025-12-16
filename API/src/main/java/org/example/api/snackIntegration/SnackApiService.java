package org.example.api.snackIntegration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Servicio mejorado para integración con Snack API
 * Llama directamente a /api/v1/codes/generate cuando un curso alcanza 10 vistas
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SnackApiService {

    private final RestTemplate restTemplate;

    @Value("${snack.api.base-url}")
    private String snackApiBaseUrl;

    /**
     * Llama a Snack para generar un código de descuento
     * Snack internamente validará que nuestro endpoint retorne 10
     *
     * @return El código generado por Snack
     */
    public String generarCodigoDesdeSnack() {
        try {
            log.info("📞 Llamando a Snack API para generar código...");

            String url = snackApiBaseUrl + "/api/v1/codes/generate";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String codigo = (String) response.getBody().get("code");
                log.info("✅ Código generado exitosamente: {}", codigo);
                return codigo;
            } else {
                log.error("❌ Error al generar código. Status: {}", response.getStatusCode());
                return null;
            }

        } catch (Exception e) {
            log.error("❌ Excepción al llamar a Snack API: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Verifica conectividad básica con Snack
     */
    public boolean verificarConectividad() {
        try {
            String url = snackApiBaseUrl + "/api/v1/codes/generate";
            restTemplate.headForHeaders(url);
            return true;
        } catch (Exception e) {
            log.error("No se puede conectar con Snack API: {}", e.getMessage());
            return false;
        }
    }
}