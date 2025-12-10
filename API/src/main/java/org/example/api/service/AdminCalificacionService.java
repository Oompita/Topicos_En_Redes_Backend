package org.example.api.service;

import lombok.RequiredArgsConstructor;
import org.example.api.dto.CalificacionResponse;
import org.example.api.exception.ResourceNotFoundException;
import org.example.api.model.Calificacion;
import org.example.api.repository.CalificacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCalificacionService {

    private final CalificacionRepository calificacionRepository;

    /**
     * Obtener todas las calificaciones con filtros opcionales
     */
    public List<CalificacionResponse> obtenerCalificaciones(
            Long cursoId,
            Long usuarioId,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta) {

        List<Calificacion> calificaciones;

        if (cursoId != null || usuarioId != null || fechaDesde != null || fechaHasta != null) {
            // Buscar con filtros
            calificaciones = calificacionRepository.buscarConFiltros(
                    cursoId, usuarioId, fechaDesde, fechaHasta
            );
        } else {
            // Sin filtros, retornar todas
            calificaciones = calificacionRepository.findAll();
        }

        return calificaciones.stream()
                .map(this::convertirACalificacionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Eliminar una calificación (solo admin)
     */
    @Transactional
    public void eliminarCalificacion(Long id) {
        Calificacion calificacion = calificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Calificación no encontrada"));

        calificacionRepository.delete(calificacion);
    }

    private CalificacionResponse convertirACalificacionResponse(Calificacion calificacion) {
        return CalificacionResponse.builder()
                .id(calificacion.getId())
                .usuarioId(calificacion.getUsuario().getId())
                .nombreUsuario(calificacion.getUsuario().getNombre() + " " + calificacion.getUsuario().getApellido())
                .cursoId(calificacion.getCurso().getId())
                .cursoTitulo(calificacion.getCurso().getTitulo())
                .puntuacion(calificacion.getPuntuacion())
                .fechaCreacion(calificacion.getFechaCreacion())
                .fechaModificacion(calificacion.getFechaModificacion())
                .build();
    }
}