package org.example.api.service;

import lombok.RequiredArgsConstructor;
import org.example.api.dto.CursoRequest;
import org.example.api.dto.CursoResponse;
import org.example.api.dto.VideoResponse;
import org.example.api.exception.BadRequestException;
import org.example.api.exception.ResourceNotFoundException;
import org.example.api.model.Categoria;
import org.example.api.model.Curso;
import org.example.api.model.Usuario;
import org.example.api.model.Video;
import org.example.api.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CursoService {

    private final CursoRepository cursoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final VideoRepository videoRepository;
    private final VisualizacionRepository visualizacionRepository;

    @Transactional
    public CursoResponse crearCurso(CursoRequest request) {
        Usuario instructor = getUsuarioAutenticado();

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        Curso curso = new Curso();
        curso.setTitulo(request.getTitulo());
        curso.setDescripcion(request.getDescripcion());
        curso.setInstructor(instructor);
        curso.setCategoria(categoria);
        curso.setImagenPortada(request.getImagenPortada());
        curso.setPublicado(false);

        curso = cursoRepository.save(curso);

        return convertirACursoResponse(curso);
    }

    @Transactional
    public CursoResponse actualizarCurso(Long id, CursoRequest request) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));

        Usuario usuarioAutenticado = getUsuarioAutenticado();
        if (!curso.getInstructor().getId().equals(usuarioAutenticado.getId())) {
            throw new BadRequestException("No tienes permisos para editar este curso");
        }

        if (request.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
            curso.setCategoria(categoria);
        }

        curso.setTitulo(request.getTitulo());
        curso.setDescripcion(request.getDescripcion());
        if (request.getImagenPortada() != null) {
            curso.setImagenPortada(request.getImagenPortada());
        }

        curso = cursoRepository.save(curso);

        return convertirACursoResponse(curso);
    }

    @Transactional
    public void eliminarCurso(Long id) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));

        Usuario usuarioAutenticado = getUsuarioAutenticado();
        if (!curso.getInstructor().getId().equals(usuarioAutenticado.getId())) {
            throw new BadRequestException("No tienes permisos para eliminar este curso");
        }

        cursoRepository.delete(curso);
    }

    @Transactional
    public CursoResponse publicarCurso(Long id) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));

        Usuario usuarioAutenticado = getUsuarioAutenticado();
        if (!curso.getInstructor().getId().equals(usuarioAutenticado.getId())) {
            throw new BadRequestException("No tienes permisos para publicar este curso");
        }

        // ✅ Cargar videos explícitamente
        List<Video> videos = videoRepository.findByCursoIdOrderByOrdenAsc(id);
        if (videos.isEmpty()) {
            throw new BadRequestException("El curso debe tener al menos un video para ser publicado");
        }

        curso.setPublicado(true);
        curso = cursoRepository.save(curso);

        return convertirACursoResponse(curso);
    }

    @Transactional(readOnly = true)
    public List<CursoResponse> obtenerCursosPublicos() {
        List<Curso> cursos = cursoRepository.findByPublicadoTrue();
        return cursos.stream()
                .map(this::convertirACursoResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CursoResponse obtenerCursoPorId(Long id) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));
        return convertirACursoResponseCompleto(curso);
    }

    @Transactional(readOnly = true)
    public List<CursoResponse> obtenerCursosPorCategoria(Long categoriaId) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        return cursoRepository.findByCategoriaAndPublicadoTrue(categoria).stream()
                .map(this::convertirACursoResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CursoResponse> buscarCursos(String keyword, Long categoriaId) {
        List<Curso> cursos;
        if (categoriaId != null) {
            cursos = cursoRepository.buscarCursosPorCategoria(keyword, categoriaId);
        } else {
            cursos = cursoRepository.buscarCursos(keyword);
        }

        return cursos.stream()
                .map(this::convertirACursoResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CursoResponse> obtenerMisCursos() {
        Usuario instructor = getUsuarioAutenticado();
        return cursoRepository.findByInstructor(instructor).stream()
                .map(this::convertirACursoResponse)
                .collect(Collectors.toList());
    }

    private Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Usuario) authentication.getPrincipal();
    }

    private CursoResponse convertirACursoResponse(Curso curso) {
        // Cargar videos solo cuando sea necesario
        List<Video> videos = videoRepository.findByCursoIdOrderByOrdenAsc(curso.getId());

        // Obtener total de vistas del curso
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
                .duracion(calcularDuracionTotal(videos))
                .totalVistas(totalVistas)
                .build();
    }

    private CursoResponse convertirACursoResponseCompleto(Curso curso) {
        // Cargar videos explícitamente
        List<Video> videos = videoRepository.findByCursoIdOrderByOrdenAsc(curso.getId());

        // Obtener total de vistas del curso
        Long totalVistas = visualizacionRepository.countByCursoId(curso.getId());

        CursoResponse response = CursoResponse.builder()
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
                .duracion(calcularDuracionTotal(videos))
                .totalVistas(totalVistas)
                .build();

        List<VideoResponse> videoResponses = videos.stream()
                .map(video -> {
                    // Obtener vistas de cada video
                    Long vistasVideo = visualizacionRepository.countByVideoId(video.getId());

                    return VideoResponse.builder()
                            .id(video.getId())
                            .titulo(video.getTitulo())
                            .descripcion(video.getDescripcion())
                            .urlVideo(video.getUrlVideo())
                            .numero(video.getOrden())
                            .duracion(video.getDuracionFormateada())
                            .fechaSubida(video.getFechaSubida())
                            .totalVistas(vistasVideo)
                            .build();
                })
                .collect(Collectors.toList());

        response.setListaVideos(videoResponses);
        return response;
    }

    private String calcularDuracionTotal(List<Video> videos) {
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