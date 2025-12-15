package org.example.api.upbolisIntegration;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller para testing y administración de la integración con UPBolis API
 * Solo accesible por administradores
 */
@RestController
@RequestMapping("/api/admin/upbolis")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UpbolisTestController {

    private final UpbolisApiService upbolisApiService;

    /**
     * Verificar conectividad con la API de UPBolis
     * GET /api/admin/upbolis/test-connection
     */
    @GetMapping("/test-connection")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean connected = upbolisApiService.verificarConectividad();

            if (connected) {
                response.put("status", "success");
                response.put("message", "Conexión exitosa con UPBolis API");
                response.put("connected", true);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "No se pudo conectar con UPBolis API");
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
     * Registrar un curso de prueba en UPBolis
     * POST /api/admin/upbolis/test-register?cursoId=1&nombre=Test&descripcion=Desc&precio=99.99
     */
    @PostMapping("/test-register")
    public ResponseEntity<Map<String, Object>> testRegisterCurso(
            @RequestParam Long cursoId,
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam Double precio) {

        Map<String, Object> response = new HashMap<>();

        try {
            UpbolisCursoResponse result = upbolisApiService.registrarCurso(
                    cursoId, nombre, descripcion, precio
            );

            if ("success".equals(result.getStatus())) {
                response.put("status", "success");
                response.put("message", "Curso registrado exitosamente en UPBolis");
                response.put("result", result);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "warning");
                response.put("message", result.getMessage());
                response.put("result", result);
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error al registrar curso: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Actualizar precio de un curso en UPBolis
     * PUT /api/admin/upbolis/update-price?cursoId=1&precio=149.99
     */
    @PutMapping("/update-price")
    public ResponseEntity<Map<String, Object>> testUpdatePrice(
            @RequestParam Long cursoId,
            @RequestParam Double precio) {

        Map<String, Object> response = new HashMap<>();

        try {
            UpbolisCursoResponse result = upbolisApiService.actualizarPrecioCurso(cursoId, precio);

            response.put("status", "success");
            response.put("message", "Precio actualizado");
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error al actualizar precio: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Eliminar un curso de UPBolis
     * DELETE /api/admin/upbolis/delete?cursoId=1
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> testDeleteCurso(@RequestParam Long cursoId) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean deleted = upbolisApiService.eliminarCurso(cursoId);

            if (deleted) {
                response.put("status", "success");
                response.put("message", "Curso eliminado exitosamente de UPBolis");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "No se pudo eliminar el curso de UPBolis");
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error al eliminar curso: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}