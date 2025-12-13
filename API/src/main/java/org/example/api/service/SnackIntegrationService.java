package org.example.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.api.dto.SnackCuponRequest;
import org.example.api.dto.SnackCuponResponse;
import org.example.api.model.Cupon;
import org.example.api.model.Curso;
import org.example.api.repository.CuponRepository;
import org.example.api.repository.CursoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SnackIntegrationService {

    private final RestTemplate restTemplate;
    private final CuponRepository cuponRepository;
    private final CursoRepository cursoRepository;

    @Value("${snack.api.url:http://localhost:8081}")
    private String snackApiUrl;

    @Value("${snack.api.key:}")
    private String snackApiKey;

    /**
     * Solicitar cupón a la API de Snack cuando un curso alcanza un umbral de vistas
     */
    @Transactional
    public Cupon solicitarCuponSnack(Long cursoId, Integer vistasAlcanzadas) {
        try {
            Curso curso = cursoRepository.findById(cursoId)
                    .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

            // Verificar si ya existe cupón para este umbral
            if (cuponRepository.existsByCursoAndVistasRequeridas(curso, vistasAlcanzadas)) {
                log.info("Ya existe un cupón para el curso {} con {} vistas", cursoId, vistasAlcanzadas);
                return null;
            }

            log.info("Solicitando cupón a Snack para curso {} con {} vistas", cursoId, vistasAlcanzadas);

            // Construir request para Snack
            SnackCuponRequest request = new SnackCuponRequest();
            request.setCursoId(cursoId);
            request.setNombreCurso(curso.getTitulo());
            request.setTotalVistas(vistasAlcanzadas.longValue());
            request.setUmbralVistas(vistasAlcanzadas);
            request.setSistemaOrigen("UPBmy");

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (snackApiKey != null && !snackApiKey.isEmpty()) {
                headers.set("X-API-Key", snackApiKey);
            }

            HttpEntity<SnackCuponRequest> entity = new HttpEntity<>(request, headers);

            // Llamar a la API de Snack
            ResponseEntity<SnackCuponResponse> response = restTemplate.exchange(
                    snackApiUrl + "/api/cupones/generar-desde-upbmy",
                    HttpMethod.POST,
                    entity,
                    SnackCuponResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                SnackCuponResponse snackResponse = response.getBody();

                log.info("Cupón recibido de Snack: {}", snackResponse.getCodigoCupon());

                // Guardar cupón en nuestra base de datos
                Cupon cupon = new Cupon();
                cupon.setCurso(curso);
                cupon.setCodigoCupon(snackResponse.getCodigoCupon());
                cupon.setVistasRequeridas(vistasAlcanzadas);
                cupon.setPorcentajeDescuento(snackResponse.getPorcentajeDescuento());
                cupon.setActivo(true);
                cupon.setUsado(false);

                // Si Snack envía fecha de expiración
                if (snackResponse.getFechaExpiracion() != null) {
                    cupon.setFechaExpiracion(snackResponse.getFechaExpiracion());
                }

                cupon = cuponRepository.save(cupon);

                log.info("Cupón guardado exitosamente en UPBmy: {}", cupon.getId());

                return cupon;
            }

        } catch (Exception e) {
            log.error("Error al solicitar cupón a Snack: {}", e.getMessage(), e);
            // No lanzamos excepción para no romper el flujo principal
        }

        return null;
    }

    /**
     * Marcar un cupón como usado
     */
    @Transactional
    public boolean marcarCuponComoUsado(String codigoCupon, Long usuarioId) {
        try {
            Cupon cupon = cuponRepository.findByCodigoCupon(codigoCupon)
                    .orElseThrow(() -> new RuntimeException("Cupón no encontrado"));

            if (cupon.isVigente()) {
                cupon.marcarComoUsado(usuarioId);
                cuponRepository.save(cupon);

                log.info("Cupón {} marcado como usado por usuario {}", codigoCupon, usuarioId);

                // Opcionalmente, notificar a Snack que el cupón fue usado
                notificarUsoACuponSnack(codigoCupon, usuarioId);

                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("Error al marcar cupón como usado: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Notificar a Snack que un cupón fue usado (opcional)
     */
    private void notificarUsoACuponSnack(String codigoCupon, Long usuarioId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (snackApiKey != null && !snackApiKey.isEmpty()) {
                headers.set("X-API-Key", snackApiKey);
            }

            String url = snackApiUrl + "/api/cupones/" + codigoCupon + "/marcar-usado";

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);

            log.info("Notificación de uso enviada a Snack para cupón {}", codigoCupon);

        } catch (Exception e) {
            log.warn("No se pudo notificar uso a Snack: {}", e.getMessage());
        }
    }
}