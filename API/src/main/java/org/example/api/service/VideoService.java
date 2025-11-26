package org.example.api.service;

import lombok.RequiredArgsConstructor;
import org.example.api.dto.VideoRequest;
import org.example.api.dto.VideoResponse;
import org.example.api.exception.BadRequestException;
import org.example.api.exception.ResourceNotFoundException;
import org.example.api.model.Curso;
import org.example.api.model.Usuario;
import org.example.api.model.Video;
import org.example.api.repository.CursoRepository;
import org.example.api.repository.VideoRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final CursoRepository cursoRepository;
    private final StorageService storageService;

    @Transactional
    public VideoResponse agregarVideo(Long cursoId, VideoRequest request, MultipartFile archivo) {
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));

        Usuario usuarioAutenticado = getUsuarioAutenticado();
        if (!curso.getInstructor().getId().equals(usuarioAutenticado.getId())) {
            throw new BadRequestException("No tienes permisos para agregar videos a este curso");
        }

        // ✅ VALIDACIÓN: Verificar que el orden no esté duplicado
        if (videoRepository.existsByCursoIdAndOrden(cursoId, request.getOrden())) {
            throw new BadRequestException(
                    "Ya existe un video con el orden " + request.getOrden() + " en este curso. " +
                            "Por favor, elige otro número de orden."
            );
        }

        String urlVideo = storageService.guardarVideo(archivo);

        Video video = new Video();
        video.setTitulo(request.getTitulo());
        video.setDescripcion(request.getDescripcion());
        video.setCurso(curso);
        video.setUrlVideo(urlVideo);
        video.setOrden(request.getOrden());
        video.setDuracionSegundos(request.getDuracionSegundos());

        video = videoRepository.save(video);

        return convertirAVideoResponse(video);
    }

    @Transactional
    public VideoResponse actualizarVideo(Long id, VideoRequest request) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video no encontrado"));

        Usuario usuarioAutenticado = getUsuarioAutenticado();
        if (!video.getCurso().getInstructor().getId().equals(usuarioAutenticado.getId())) {
            throw new BadRequestException("No tienes permisos para editar este video");
        }

        // ✅ VALIDACIÓN: Verificar orden solo si cambió
        if (!video.getOrden().equals(request.getOrden())) {
            if (videoRepository.existsByCursoIdAndOrden(video.getCurso().getId(), request.getOrden())) {
                throw new BadRequestException(
                        "Ya existe un video con el orden " + request.getOrden() + " en este curso. " +
                                "Por favor, elige otro número de orden."
                );
            }
        }

        video.setTitulo(request.getTitulo());
        video.setDescripcion(request.getDescripcion());
        video.setOrden(request.getOrden());
        if (request.getDuracionSegundos() != null) {
            video.setDuracionSegundos(request.getDuracionSegundos());
        }

        video = videoRepository.save(video);

        return convertirAVideoResponse(video);
    }

    @Transactional
    public void eliminarVideo(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video no encontrado"));

        Usuario usuarioAutenticado = getUsuarioAutenticado();
        if (!video.getCurso().getInstructor().getId().equals(usuarioAutenticado.getId())) {
            throw new BadRequestException("No tienes permisos para eliminar este video");
        }

        storageService.eliminarArchivo(video.getUrlVideo());
        videoRepository.delete(video);
    }

    public List<VideoResponse> obtenerVideosPorCurso(Long cursoId) {
        List<Video> videos = videoRepository.findByCursoIdOrderByOrdenAsc(cursoId);
        return videos.stream()
                .map(this::convertirAVideoResponse)
                .collect(Collectors.toList());
    }

    public VideoResponse obtenerVideoPorId(Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video no encontrado"));
        return convertirAVideoResponse(video);
    }

    private Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Usuario) authentication.getPrincipal();
    }

    private VideoResponse convertirAVideoResponse(Video video) {
        return VideoResponse.builder()
                .id(video.getId())
                .titulo(video.getTitulo())
                .descripcion(video.getDescripcion())
                .urlVideo(video.getUrlVideo())
                .numero(video.getOrden())
                .duracion(video.getDuracionFormateada())
                .fechaSubida(video.getFechaSubida())
                .build();
    }
}