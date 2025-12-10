package org.example.api.service;

import lombok.RequiredArgsConstructor;
import org.example.api.dto.CalificacionRequest;
import org.example.api.dto.CalificacionResponse;
import org.example.api.dto.RatingResumen;
import org.example.api.exception.BadRequestException;
import org.example.api.exception.ResourceNotFoundException;
import org.example.api.model.Calificacion;
import org.example.api.model.Curso;
import org.example.api.model.Usuario;
import org.example.api.repository.CalificacionRepository;
import org.example.api.repository.CursoRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CalificacionService {

    private final CalificacionRepository calificacionRepository;
    private final CursoRepository cursoRepository;

    /**
     * Calificar o actualizar calificación de un curso
     * Si el usuario ya calificó, actualiza la calificación existente
     */
    @Transactional
    public CalificacionResponse calificarCurso(Long cursoId, CalificacionRequest request) {
        Usuario usuario = getUsuarioAutenticado();

        // Validar que el curso existe
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));

        // Validar que no es el instructor calificando su propio curso
        if (curso.getInstructor().getId().equals(usuario.getId())) {
            throw new BadRequestException("No puedes calificar tu propio curso");
        }

        // Buscar si ya existe una calificación
        Optional<Calificacion> calificacionExistente =
                calificacionRepository.findByUsuarioIdAndCursoId(usuario.getId(), cursoId);

        Calificacion calificacion;

        if (calificacionExistente.isPresent()) {
            // Actualizar calificación existente
            calificacion = calificacionExistente.get();
            calificacion.setPuntuacion(request.getPuntuacion());
        } else {
            // Crear nueva calificación
            calificacion = new Calificacion();
            calificacion.setUsuario(usuario);
            calificacion.setCurso(curso);
            calificacion.setPuntuacion(request.getPuntuacion());
        }

        calificacion = calificacionRepository.save(calificacion);

        return convertirACalificacionResponse(calificacion);
    }

    /**
     * Obtener calificación de un usuario para un curso específico
     */
    public CalificacionResponse obtenerCalificacionUsuario(Long cursoId) {
        Usuario usuario = getUsuarioAutenticado();

        Calificacion calificacion = calificacionRepository
                .findByUsuarioIdAndCursoId(usuario.getId(), cursoId)
                .orElse(null);

        return calificacion != null ? convertirACalificacionResponse(calificacion) : null;
    }

    /**
     * Obtener resumen de calificaciones de un curso (promedio, total, distribución)
     */
    public RatingResumen obtenerResumenCalificaciones(Long cursoId) {
        // Validar que el curso existe
        if (!cursoRepository.existsById(cursoId)) {
            throw new ResourceNotFoundException("Curso no encontrado");
        }

        Double promedio = calificacionRepository.obtenerPromedioCalificacion(cursoId);
        Long total = calificacionRepository.countByCursoId(cursoId);

        // Si no hay calificaciones, retornar valores por defecto
        if (total == 0 || promedio == null) {
            return RatingResumen.builder()
                    .cursoId(cursoId)
                    .promedioCalificacion(0.0)
                    .totalCalificaciones(0L)
                    .estrellas5(0L)
                    .estrellas4(0L)
                    .estrellas3(0L)
                    .estrellas2(0L)
                    .estrellas1(0L)
                    .build();
        }

        // Obtener distribución de estrellas
        return RatingResumen.builder()
                .cursoId(cursoId)
                .promedioCalificacion(Math.round(promedio * 10.0) / 10.0) // Redondear a 1 decimal
                .totalCalificaciones(total)
                .estrellas5(calificacionRepository.countByCursoIdAndPuntuacion(cursoId, 5))
                .estrellas4(calificacionRepository.countByCursoIdAndPuntuacion(cursoId, 4))
                .estrellas3(calificacionRepository.countByCursoIdAndPuntuacion(cursoId, 3))
                .estrellas2(calificacionRepository.countByCursoIdAndPuntuacion(cursoId, 2))
                .estrellas1(calificacionRepository.countByCursoIdAndPuntuacion(cursoId, 1))
                .build();
    }

    /**
     * Eliminar calificación de un usuario (opcional)
     */
    @Transactional
    public void eliminarCalificacion(Long cursoId) {
        Usuario usuario = getUsuarioAutenticado();

        Calificacion calificacion = calificacionRepository
                .findByUsuarioIdAndCursoId(usuario.getId(), cursoId)
                .orElseThrow(() -> new ResourceNotFoundException("No has calificado este curso"));

        calificacionRepository.delete(calificacion);
    }

    private Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Usuario) authentication.getPrincipal();
    }

    private CalificacionResponse convertirACalificacionResponse(Calificacion calificacion) {
        return CalificacionResponse.builder()
                .id(calificacion.getId())
                .usuarioId(calificacion.getUsuario().getId())
                .nombreUsuario(calificacion.getUsuario().getNombre() + " " + calificacion.getUsuario().getApellido())
                .cursoId(calificacion.getCurso().getId())
                .puntuacion(calificacion.getPuntuacion())
                .fechaCreacion(calificacion.getFechaCreacion())
                .fechaModificacion(calificacion.getFechaModificacion())
                .build();
    }
}