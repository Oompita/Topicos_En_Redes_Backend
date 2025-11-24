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
import org.example.api.repository.CategoriaRepository;
import org.example.api.repository.CursoRepository;
import org.example.api.repository.UsuarioRepository;
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

        if (curso.getVideos() == null || curso.getVideos().isEmpty()) {
            throw new BadRequestException("El curso debe tener al menos un video para ser publicado");
        }

        curso.setPublicado(true);
        curso = cursoRepository.save(curso);

        return convertirACursoResponse(curso);
    }

    public List<CursoResponse> obtenerCursosPublicos() {
        return cursoRepository.findByPublicadoTrue().stream()
                .map(this::convertirACursoResponse)
                .collect(Collectors.toList());
    }

    public CursoResponse obtenerCursoPorId(Long id) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));
        return convertirACursoResponseCompleto(curso);
    }

    public List<CursoResponse> obtenerCursosPorCategoria(Long categoriaId) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        return cursoRepository.findByCategoriaAndPublicadoTrue(categoria).stream()
                .map(this::convertirACursoResponse)
                .collect(Collectors.toList());
    }

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
                .videos(curso.getCantidadVideos())
                .duracion(curso.getDuracionTotal())
                .build();
    }

    private CursoResponse convertirACursoResponseCompleto(Curso curso) {
        CursoResponse response = convertirACursoResponse(curso);

        if (curso.getVideos() != null) {
            List<VideoResponse> videos = curso.getVideos().stream()
                    .map(video -> VideoResponse.builder()
                            .id(video.getId())
                            .titulo(video.getTitulo())
                            .descripcion(video.getDescripcion())
                            .urlVideo(video.getUrlVideo())
                            .numero(video.getOrden())
                            .duracion(video.getDuracionFormateada())
                            .fechaSubida(video.getFechaSubida())
                            .build())
                    .collect(Collectors.toList());
            response.setListaVideos(videos);
        }

        return response;
    }
}
