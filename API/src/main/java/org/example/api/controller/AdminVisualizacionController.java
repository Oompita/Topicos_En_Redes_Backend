package org.example.api.controller;

import lombok.RequiredArgsConstructor;
import org.example.api.dto.VisualizacionResponse;
import org.example.api.service.AdminVisualizacionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/visualizaciones")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminVisualizacionController {

    private final AdminVisualizacionService adminVisualizacionService;

    /**
     * Obtener todas las visualizaciones con filtros opcionales
     * GET /api/admin/visualizaciones?videoId=1&usuarioId=2&fechaDesde=2024-01-01&fechaHasta=2024-12-31
     */
    @GetMapping
    public ResponseEntity<List<VisualizacionResponse>> obtenerVisualizaciones(
            @RequestParam(required = false) Long videoId,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta) {

        List<VisualizacionResponse> visualizaciones = adminVisualizacionService.obtenerVisualizaciones(
                videoId, usuarioId, fechaDesde, fechaHasta
        );

        return ResponseEntity.ok(visualizaciones);
    }

    /**
     * Eliminar una visualización específica (solo admin)
     * DELETE /api/admin/visualizaciones/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarVisualizacion(@PathVariable Long id) {
        adminVisualizacionService.eliminarVisualizacion(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtener estadísticas de visualizaciones
     * GET /api/admin/visualizaciones/estadisticas
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Long> obtenerEstadisticasVisualizaciones() {
        Long total = adminVisualizacionService.obtenerTotalVisualizaciones();
        return ResponseEntity.ok(total);
    }
}