package org.example.api.controller;

import lombok.RequiredArgsConstructor;
import org.example.api.dto.CalificacionResponse;
import org.example.api.service.AdminCalificacionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/calificaciones")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCalificacionController {

    private final AdminCalificacionService adminCalificacionService;

    /**
     * Obtener todas las calificaciones con filtros opcionales
     * GET /api/admin/calificaciones?cursoId=1&usuarioId=2&fechaDesde=2024-01-01&fechaHasta=2024-12-31
     */
    @GetMapping
    public ResponseEntity<List<CalificacionResponse>> obtenerCalificaciones(
            @RequestParam(required = false) Long cursoId,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {

        List<CalificacionResponse> calificaciones = adminCalificacionService.obtenerCalificaciones(
                cursoId, usuarioId, fechaDesde, fechaHasta
        );

        return ResponseEntity.ok(calificaciones);
    }

    /**
     * Eliminar una calificación específica (solo admin)
     * DELETE /api/admin/calificaciones/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCalificacion(@PathVariable Long id) {
        adminCalificacionService.eliminarCalificacion(id);
        return ResponseEntity.noContent().build();
    }
}