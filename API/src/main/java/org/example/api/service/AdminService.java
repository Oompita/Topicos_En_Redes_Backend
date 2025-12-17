package org.example.api.service;

import lombok.RequiredArgsConstructor;
import org.example.api.dto.CursoResponse;
import org.example.api.dto.UsuarioRequest;
import org.example.api.dto.UsuarioResponse;
import org.example.api.exception.BadRequestException;
import org.example.api.exception.ResourceNotFoundException;
import org.example.api.model.Curso;
import org.example.api.model.Usuario;
import org.example.api.model.Video;
import org.example.api.repository.*;
import org.example.api.upbolisIntegration.UpbolisApiService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;
    private final VideoRepository videoRepository;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;
    private final CalificacionRepository calificacionRepository;
    private final CategoriaRepository categoriaRepository;
    private final VisualizacionRepository visualizacionRepository;
    private final UpbolisApiService upbolisApiService;

    // ==================== GESTIÓN DE USUARIOS ====================

    @Transactional(readOnly = true)
    public List<UsuarioResponse> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::convertirAUsuarioResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerUsuarioPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return convertirAUsuarioResponse(usuario);
    }

    @Transactional
    public UsuarioResponse crearUsuario(UsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("El email ya está registrado");
        }

        // Generar contraseña temporal
        String passwordTemporal = UUID.randomUUID().toString().substring(0, 8);

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(passwordTemporal));
        usuario.setRol(request.getRol());
        usuario.setActivo(request.getActivo() != null ? request.getActivo() : true);

        usuario = usuarioRepository.save(usuario);

        // En producción, aquí enviarías un email con la contraseña temporal
        System.out.println("Contraseña temporal para " + request.getEmail() + ": " + passwordTemporal);

        return convertirAUsuarioResponse(usuario);
    }

    @Transactional
    public UsuarioResponse actualizarUsuario(Long id, UsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!usuario.getEmail().equals(request.getEmail()) &&
                usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("El email ya está en uso");
        }

        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setEmail(request.getEmail());
        usuario.setRol(request.getRol());
        usuario.setActivo(request.getActivo());

        usuario = usuarioRepository.save(usuario);
        return convertirAUsuarioResponse(usuario);
    }

    @Transactional
    public void cambiarEstadoUsuario(Long id, Boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Verificar si tiene cursos
        if (usuario.getCursosCreados() != null && !usuario.getCursosCreados().isEmpty()) {
            throw new BadRequestException("No se puede eliminar un usuario con cursos creados. Primero elimina sus cursos.");
        }

        usuarioRepository.delete(usuario);
    }

    // ==================== GESTIÓN DE CURSOS ====================

    @Transactional(readOnly = true)
    public List<CursoResponse> obtenerTodosLosCursos() {
        return cursoRepository.findAll().stream()
                .map(this::convertirACursoResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminarCursoAdmin(Long id) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));

        // Eliminar videos y archivos asociados
        List<Video> videos = videoRepository.findByCursoIdOrderByOrdenAsc(id);
        for (Video video : videos) {
            storageService.eliminarArchivo(video.getUrlVideo());
        }

        // Eliminar imagen de portada si existe
        if (curso.getImagenPortada() != null) {
            storageService.eliminarArchivo(curso.getImagenPortada());
        }

        if (curso.getPublicado()) {
            try {
                upbolisApiService.desactivarProducto(curso.getId());
            } catch (Exception e) {
                System.err.println("Error al eliminar curso de UPBolis: " + e.getMessage());
            }
        }

        cursoRepository.delete(curso);
    }

    @Transactional
    public CursoResponse cambiarEstadoCurso(Long id, Boolean publicado) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));

        curso.setPublicado(publicado);
        curso = cursoRepository.save(curso);

        return convertirACursoResponse(curso);
    }

    // ==================== GESTIÓN DE VIDEOS ====================

    @Transactional
    public void eliminarVideoAdmin(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video no encontrado"));

        storageService.eliminarArchivo(video.getUrlVideo());
        videoRepository.delete(video);
    }

    // ==================== ESTADÍSTICAS ====================

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsuarios = usuarioRepository.count();
        long totalCursos = cursoRepository.count();
        long cursosPublicados = cursoRepository.findByPublicadoTrue().size();
        long totalVideos = videoRepository.count();

        long estudiantes = usuarioRepository.findAll().stream()
                .filter(u -> u.getRol().name().equals("ESTUDIANTE"))
                .count();

        long instructores = usuarioRepository.findAll().stream()
                .filter(u -> u.getRol().name().equals("INSTRUCTOR"))
                .count();

        long totalCategorias = categoriaRepository.count();
        long totalCalificaciones = calificacionRepository.count();

        // 🆕 Agregar total de visualizaciones
        long totalVisualizaciones = visualizacionRepository.contarTotalVisualizaciones();

        stats.put("totalUsuarios", totalUsuarios);
        stats.put("totalCursos", totalCursos);
        stats.put("cursosPublicados", cursosPublicados);
        stats.put("cursosBorrador", totalCursos - cursosPublicados);
        stats.put("totalVideos", totalVideos);
        stats.put("estudiantes", estudiantes);
        stats.put("instructores", instructores);
        stats.put("totalCategorias", totalCategorias);
        stats.put("totalCalificaciones", totalCalificaciones);
        stats.put("totalVisualizaciones", totalVisualizaciones);

        return stats;
    }

    // ==================== MÉTODOS AUXILIARES ====================

    private UsuarioResponse convertirAUsuarioResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .activo(usuario.getActivo())
                .fechaRegistro(usuario.getFechaRegistro())
                .cursosCreados(usuario.getCursosCreados() != null ? usuario.getCursosCreados().size() : 0)
                .build();
    }

    private CursoResponse convertirACursoResponse(Curso curso) {
        List<Video> videos = videoRepository.findByCursoIdOrderByOrdenAsc(curso.getId());

        // 🆕 Obtener total de vistas del curso
        Long totalVistas = visualizacionRepository.countByCursoId(curso.getId());

        return CursoResponse.builder()
                .id(curso.getId())
                .titulo(curso.getTitulo())
                .descripcion(curso.getDescripcion())
                .instructor(curso.getInstructor().getNombre() + " " + curso.getInstructor().getApellido())
                .instructorId(curso.getInstructor().getId())
                .categoria(curso.getCategoria().getNombre())
                .categoriaId(curso.getCategoria().getId())
                .imagenPortada(curso.getImagenPortada())
                .fechaCreacion(curso.getFechaCreacion())
                .publicado(curso.getPublicado())
                .videos(videos.size())
                .duracion(calcularDuracion(videos))
                .totalVistas(totalVistas)
                .precio(curso.getPrecio())
                .build();
    }

    private String calcularDuracion(List<Video> videos) {
        if (videos == null || videos.isEmpty()) {
            return "0 horas";
        }

        int totalMinutos = videos.stream()
                .filter(v -> v.getDuracionSegundos() != null)
                .mapToInt(v -> (int) Math.ceil(v.getDuracionSegundos() / 60.0))
                .sum();

        int horas = totalMinutos / 60;
        int minutos = totalMinutos % 60;

        if (horas > 0 && minutos > 0) {
            return horas + "h " + minutos + "m";
        } else if (horas > 0) {
            return horas + " horas";
        } else {
            return minutos + " minutos";
        }
    }


}