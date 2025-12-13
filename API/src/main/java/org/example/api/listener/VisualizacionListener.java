package org.example.api.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.api.model.Cupon;
import org.example.api.model.Video;
import org.example.api.repository.VisualizacionRepository;
import org.example.api.service.SnackIntegrationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener que detecta cuando un curso alcanza ciertos umbrales de vistas
 * y solicita automáticamente cupones a Snack
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VisualizacionListener {

    private final SnackIntegrationService snackIntegrationService;
    private final VisualizacionRepository visualizacionRepository;

    // Umbrales de vistas para generar cupones
    // Puedes agregar más: 10, 50, 100, 500, 1000, 5000
    private static final Integer[] UMBRALES = {10, 50, 100, 500, 1000};

    /**
     * Escucha cuando se crea una nueva visualización
     * Se ejecuta después de que la transacción se complete exitosamente
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVisualizacionCreated(VisualizacionEvent event) {
        try {
            Video video = event.getVideo();
            Long cursoId = video.getCurso().getId();

            // Obtener total de vistas del curso
            Long totalVistas = visualizacionRepository.countByCursoId(cursoId);

            log.info("Curso {} tiene ahora {} visualizaciones", cursoId, totalVistas);

            // Verificar si alcanzó algún umbral
            for (Integer umbral : UMBRALES) {
                if (totalVistas.equals(umbral.longValue())) {
                    log.info("¡Umbral alcanzado! Curso {} llegó a {} vistas", cursoId, umbral);

                    // Solicitar cupón a Snack
                    Cupon cupon = snackIntegrationService.solicitarCuponSnack(cursoId, umbral);

                    if (cupon != null) {
                        log.info("✅ Cupón generado exitosamente: {} para curso {}",
                                cupon.getCodigoCupon(), cursoId);
                    }

                    break; // Solo procesar un umbral a la vez
                }
            }

        } catch (Exception e) {
            log.error("Error en VisualizacionListener: {}", e.getMessage(), e);
        }
    }
}

