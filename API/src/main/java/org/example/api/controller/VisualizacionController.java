package org.example.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.api.dto.VisualizacionResponse;
import org.example.api.service.VisualizacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visualizaciones")
@RequiredArgsConstructor
public class VisualizacionController {

    private final VisualizacionService visualizacionService;

    /**
     * Registrar una vista de un video
     * POST /api/visualizaciones/video/{videoId}
     * Público - no requiere autenticación
     */
    @PostMapping("/video/{videoId}")
    public ResponseEntity<VisualizacionResponse> registrarVista(
            @PathVariable Long videoId,
            HttpServletRequest request) {

        VisualizacionResponse response = visualizacionService.registrarVista(videoId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Obtener total de vistas de un video
     * GET /api/visualizaciones/video/{videoId}/total
     * Público
     */
    @GetMapping("/video/{videoId}/total")
    public ResponseEntity<Long> obtenerTotalVistasVideo(@PathVariable Long videoId) {
        Long total = visualizacionService.obtenerTotalVistasVideo(videoId);
        return ResponseEntity.ok(total);
    }

    /**
     * Obtener total de vistas de un curso
     * GET /api/visualizaciones/curso/{cursoId}/total
     * Público
     */
    @GetMapping("/curso/{cursoId}/total")
    public ResponseEntity<Long> obtenerTotalVistasCurso(@PathVariable Long cursoId) {
        Long total = visualizacionService.obtenerTotalVistasCurso(cursoId);
        return ResponseEntity.ok(total);
    }

    /**
     * Obtener historial de visualizaciones del usuario autenticado
     * GET /api/visualizaciones/mi-historial
     * Requiere autenticación
     */
    @GetMapping("/mi-historial")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<VisualizacionResponse>> obtenerMiHistorial() {
        List<VisualizacionResponse> historial = visualizacionService.obtenerHistorialUsuario();
        return ResponseEntity.ok(historial);
    }
}