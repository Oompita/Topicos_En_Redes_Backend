package org.example.api.snackIntegration;

import lombok.RequiredArgsConstructor;
import org.example.api.service.SnackApiService;
import org.example.api.service.VisualizacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller para testing y administración de la integración con Snack API
 * Solo accesible por administradores
 */
@RestController
@RequestMapping("/api/admin/snack")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class SnackTestController {

    private final SnackApiService snackApiService;
    private final VisualizacionService visualizacionService;

    /**
     * Verificar conectividad con la API de Snack
     * GET /api/admin/snack/test-connection
     */
    @GetMapping("/test-connection")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean connected = snackApiService.verificarConectividad();

            if (connected) {
                response.put("status", "success");
                response.put("message", "Conexión exitosa con Snack API");
                response.put("connected", true);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "No se pudo conectar con Snack API");
                response.put("connected", false);
                return ResponseEntity.status(503).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error al verificar conexión: " + e.getMessage());
            response.put("connected", false);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Simular el proceso de notificación a Snack (para testing)
     * POST /api/admin/snack/test-notify?cursoId=1&views=10
     */
    @PostMapping("/test-notify")
    public ResponseEntity<Map<String, Object>> testNotify(
            @RequestParam Long cursoId,
            @RequestParam Long views) {

        Map<String, Object> response = new HashMap<>();

        try {
            String codigo = snackApiService.notificarVistasYObtenerCodigo(cursoId, views);

            if (codigo != null) {
                response.put("status", "success");
                response.put("message", "Notificación enviada exitosamente");
                response.put("codigoRecibido", codigo);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "warning");
                response.put("message", "Notificación enviada pero no se recibió código");
                response.put("codigoRecibido", null);
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error al notificar: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Actualizar manualmente la descripción de un curso con un código de prueba
     * PUT /api/admin/snack/update-description?cursoId=1&codigo=TESTCODE123
     */
    @PutMapping("/update-description")
    public ResponseEntity<Map<String, Object>> updateDescription(
            @RequestParam Long cursoId,
            @RequestParam String codigo) {

        Map<String, Object> response = new HashMap<>();

        try {
            visualizacionService.actualizarDescripcionConCodigo(cursoId, codigo);

            response.put("status", "success");
            response.put("message", "Descripción actualizada exitosamente");
            response.put("cursoId", cursoId);
            response.put("codigo", codigo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error al actualizar descripción: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}