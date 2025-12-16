package org.example.api.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.api.dto.VisualizacionResponse;
import org.example.api.exception.ResourceNotFoundException;
import org.example.api.model.Curso;
import org.example.api.model.Usuario;
import org.example.api.model.Video;
import org.example.api.model.Visualizacion;
import org.example.api.repository.CursoRepository;
import org.example.api.repository.VideoRepository;
import org.example.api.repository.VisualizacionRepository;
import org.example.api.snackIntegration.SnackApiService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class VisualizacionService {

    private final VisualizacionRepository visualizacionRepository;
    private final VideoRepository videoRepository;
    private final CursoRepository cursoRepository;
    private final SnackApiService snackApiService;

    /**
     * Registrar una nueva vista de un video
     * Puede ser de un usuario autenticado o anónimo
     * Detecta cuando un curso alcanza 10 vistas por primera vez
     */
    @Transactional
    public VisualizacionResponse registrarVista(Long videoId, HttpServletRequest request) {
        // Verificar que el video existe
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video no encontrado"));

        // Obtener vistas ANTES de registrar la nueva
        Long cursoId = video.getCurso().getId();
        Long vistasAntes = visualizacionRepository.countByCursoId(cursoId);

        // Registrar la nueva visualización
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

        // Verificar si el curso acaba de alcanzar 10 vistas
        Long vistasDespues = vistasAntes + 1;

        log.info("Curso ID {}: Vistas antes={}, vistas después={}", cursoId, vistasAntes, vistasDespues);

        if (vistasAntes < 10 && vistasDespues >= 10) {
            log.info("🎉 Curso ID {} alcanzó 10 vistas! Notificando a Snack API...", cursoId);
            procesarHitoDeDiezVistas(cursoId, vistasDespues);
        }

        return convertirAVisualizacionResponse(visualizacion);
    }

    /**
     * Procesa el evento cuando un curso alcanza 10 vistas por primera vez
     * Llama directamente a Snack para generar código y actualiza la descripción
     */
    private void procesarHitoDeDiezVistas(Long cursoId, Long vistasActuales) {
        try {
            log.info("🎯 Curso {} alcanzó exactamente 10 vistas. Solicitando código a Snack...", cursoId);

            // Llamar directamente al endpoint de generación de Snack
            // Snack internamente consultará nuestro endpoint /api/snack/views-validation que retorna 10
            String codigoDescuento = snackApiService.generarCodigoDesdeSnack();

            if (codigoDescuento != null && !codigoDescuento.trim().isEmpty()) {
                log.info("✅ Código recibido de Snack: {}", codigoDescuento);
                actualizarDescripcionConCodigo(cursoId, codigoDescuento);
            } else {
                log.warn("⚠️ Snack no devolvió código para curso ID {}", cursoId);
            }

        } catch (Exception e) {
            log.error("❌ Error al procesar hito de 10 vistas para curso {}: {}", cursoId, e.getMessage(), e);
        }
    }

    /**
     * Actualiza la descripción del curso agregando el código de descuento
     * Si no existe la sección "Códigos de descuento en Snack: ", la crea
     * Si ya existe, simplemente añade el nuevo código
     */
    @Transactional
    public void actualizarDescripcionConCodigo(Long cursoId, String codigoDescuento) {
        try {
            Curso curso = cursoRepository.findById(cursoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));

            String descripcionActual = curso.getDescripcion() != null ? curso.getDescripcion() : "";
            String seccionCodigos = "\nCódigos de descuento en Snack: ";

            String nuevaDescripcion;

            if (descripcionActual.contains(seccionCodigos)) {
                // Ya existe la sección, añadir el nuevo código
                nuevaDescripcion = descripcionActual + ", " + codigoDescuento;
                log.info("📝 Añadiendo código adicional a sección existente");
            } else {
                // No existe la sección, crearla
                if (!descripcionActual.isEmpty()) {
                    nuevaDescripcion = descripcionActual + "\n\n" + seccionCodigos + codigoDescuento;
                } else {
                    nuevaDescripcion = seccionCodigos + codigoDescuento;
                }
                log.info("📝 Creando nueva sección de códigos");
            }

            curso.setDescripcion(nuevaDescripcion);
            cursoRepository.save(curso);

            log.info("✅ Descripción del curso ID {} actualizada con código: {}", cursoId, codigoDescuento);

        } catch (Exception e) {
            log.error("❌ Error al actualizar descripción del curso {}: {}", cursoId, e.getMessage(), e);
        }
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