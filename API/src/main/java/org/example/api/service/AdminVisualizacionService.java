package org.example.api.service;

import lombok.RequiredArgsConstructor;
import org.example.api.dto.VisualizacionResponse;
import org.example.api.exception.ResourceNotFoundException;
import org.example.api.model.Visualizacion;
import org.example.api.repository.VisualizacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminVisualizacionService {

    private final VisualizacionRepository visualizacionRepository;

    /**
     * Obtener todas las visualizaciones con filtros opcionales
     */
    public List<VisualizacionResponse> obtenerVisualizaciones(
            Long videoId,
            Long usuarioId,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta) {

        List<Visualizacion> visualizaciones;

        if (videoId != null || usuarioId != null || fechaDesde != null || fechaHasta != null) {
            // Buscar con filtros
            visualizaciones = visualizacionRepository.buscarConFiltros(
                    videoId, usuarioId, fechaDesde, fechaHasta
            );
        } else {
            // Sin filtros, retornar todas
            visualizaciones = visualizacionRepository.findAll();
        }

        return visualizaciones.stream()
                .map(this::convertirAVisualizacionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Eliminar una visualización (solo admin)
     */
    @Transactional
    public void eliminarVisualizacion(Long id) {
        Visualizacion visualizacion = visualizacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visualización no encontrada"));

        visualizacionRepository.delete(visualizacion);
    }

    /**
     * Obtener estadísticas de visualizaciones
     */
    public Long obtenerTotalVisualizaciones() {
        return visualizacionRepository.contarTotalVisualizaciones();
    }

    private VisualizacionResponse convertirAVisualizacionResponse(Visualizacion visualizacion) {
        return VisualizacionResponse.builder()
                .id(visualizacion.getId())
                .videoId(visualizacion.getVideo().getId())
                .videoTitulo(visualizacion.getVideo().getTitulo())
                .usuarioId(visualizacion.getUsuario() != null ? visualizacion.getUsuario().getId() : null)
                .nombreUsuario(visualizacion.getUsuario() != null
                        ? visualizacion.getUsuario().getNombre() + " " + visualizacion.getUsuario().getApellido()
                        : "Anónimo")
                .fechaVisualizacion(visualizacion.getFechaVisualizacion())
                .ipAddress(visualizacion.getIpAddress())
                .build();
    }
}