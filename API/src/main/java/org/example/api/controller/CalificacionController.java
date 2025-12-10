package org.example.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.api.dto.CalificacionRequest;
import org.example.api.dto.CalificacionResponse;
import org.example.api.dto.RatingResumen;
import org.example.api.service.CalificacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calificaciones")
@RequiredArgsConstructor
public class CalificacionController {

    private final CalificacionService calificacionService;

    /**
     * Calificar o actualizar calificación de un curso
     * POST /api/calificaciones/curso/{cursoId}
     * Body: { "puntuacion": 5 }
     */
    @PostMapping("/curso/{cursoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CalificacionResponse> calificarCurso(
            @PathVariable Long cursoId,
            @Valid @RequestBody CalificacionRequest request) {

        CalificacionResponse response = calificacionService.calificarCurso(cursoId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Obtener calificación del usuario autenticado para un curso
     * GET /api/calificaciones/curso/{cursoId}/mi-calificacion
     */
    @GetMapping("/curso/{cursoId}/mi-calificacion")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CalificacionResponse> obtenerMiCalificacion(@PathVariable Long cursoId) {
        CalificacionResponse response = calificacionService.obtenerCalificacionUsuario(cursoId);

        if (response == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Obtener resumen de calificaciones de un curso (promedio + distribución)
     * GET /api/calificaciones/curso/{cursoId}/resumen
     * Público - no requiere autenticación
     */
    @GetMapping("/curso/{cursoId}/resumen")
    public ResponseEntity<RatingResumen> obtenerResumenCalificaciones(@PathVariable Long cursoId) {
        RatingResumen resumen = calificacionService.obtenerResumenCalificaciones(cursoId);
        return ResponseEntity.ok(resumen);
    }

    /**
     * Eliminar calificación del usuario autenticado
     * DELETE /api/calificaciones/curso/{cursoId}
     */
    @DeleteMapping("/curso/{cursoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> eliminarCalificacion(@PathVariable Long cursoId) {
        calificacionService.eliminarCalificacion(cursoId);
        return ResponseEntity.noContent().build();
    }
}