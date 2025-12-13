package org.example.api.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.api.dto.VisualizacionResponse;
import org.example.api.exception.ResourceNotFoundException;
import org.example.api.listener.VisualizacionEvent;
import org.example.api.model.Usuario;
import org.example.api.model.Video;
import org.example.api.model.Visualizacion;
import org.example.api.repository.VideoRepository;
import org.example.api.repository.VisualizacionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VisualizacionService {

    private final VisualizacionRepository visualizacionRepository;
    private final VideoRepository videoRepository;
    private final ApplicationEventPublisher eventPublisher; // NUEVO

    /**
     * Registrar una nueva vista de un video
     * Puede ser de un usuario autenticado o anónimo
     */
    @Transactional
    public VisualizacionResponse registrarVista(Long videoId, HttpServletRequest request) {
        // Verificar que el video existe
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video no encontrado"));

        Visualizacion visualizacion = new Visualizacion();
        visualizacion.setVideo(video);

        // Intentar obtener el usuario autenticado (puede ser null si es anónimo)
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Usuario) {
                Usuario usuario = (Usuario) authentication.getPrincipal();
                visualizacion.setUsuario(usuario);
            }
        } catch (Exception e) {
            // Si falla, dejamos usuario como null (vista anónima)
        }

        // Obtener IP del request (opcional, para analytics futuros)
        String ipAddress = getClientIp(request);
        visualizacion.setIpAddress(ipAddress);

        visualizacion = visualizacionRepository.save(visualizacion);

        // ✅ NUEVO: Publicar evento para que el listener lo detecte
        eventPublisher.publishEvent(new VisualizacionEvent(this, video, visualizacion.getId()));

        return convertirAVisualizacionResponse(visualizacion);
    }

    /**
     * Obtener total de vistas de un video
     */
    public Long obtenerTotalVistasVideo(Long videoId) {
        return visualizacionRepository.countByVideoId(videoId);
    }

    /**
     * Obtener total de vistas de un curso (suma de vistas de todos sus videos)
     */
    public Long obtenerTotalVistasCurso(Long cursoId) {
        return visualizacionRepository.countByCursoId(cursoId);
    }

    /**
     * Obtener historial de visualizaciones del usuario autenticado (para futuro)
     */
    public List<VisualizacionResponse> obtenerHistorialUsuario() {
        Usuario usuario = getUsuarioAutenticado();

        List<Visualizacion> visualizaciones = visualizacionRepository
                .findByUsuarioIdOrderByFechaVisualizacionDesc(usuario.getId());

        return visualizaciones.stream()
                .map(this::convertirAVisualizacionResponse)
                .collect(Collectors.toList());
    }

    // Método auxiliar para obtener IP del cliente
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Usuario)) {
            throw new RuntimeException("Usuario no autenticado");
        }
        return (Usuario) authentication.getPrincipal();
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