package org.example.api.controller;

import lombok.RequiredArgsConstructor;
import org.example.api.dto.VideoRequest;
import org.example.api.dto.VideoResponse;
import org.example.api.service.VideoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @PostMapping("/curso/{cursoId}")
    public ResponseEntity<VideoResponse> agregarVideo(
            @PathVariable Long cursoId,
            @RequestParam("titulo") String titulo,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam("orden") Integer orden,
            @RequestParam(value = "duracionSegundos", required = false) Integer duracionSegundos,
            @RequestParam("archivo") MultipartFile archivo) {

        VideoRequest request = new VideoRequest();
        request.setTitulo(titulo);
        request.setDescripcion(descripcion);
        request.setOrden(orden);
        request.setDuracionSegundos(duracionSegundos);

        VideoResponse response = videoService.agregarVideo(cursoId, request, archivo);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VideoResponse> actualizarVideo(
            @PathVariable Long id,
            @RequestParam("titulo") String titulo,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam("orden") Integer orden,
            @RequestParam(value = "duracionSegundos", required = false) Integer duracionSegundos) {

        VideoRequest request = new VideoRequest();
        request.setTitulo(titulo);
        request.setDescripcion(descripcion);
        request.setOrden(orden);
        request.setDuracionSegundos(duracionSegundos);

        VideoResponse response = videoService.actualizarVideo(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarVideo(@PathVariable Long id) {
        videoService.eliminarVideo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/curso/{cursoId}")
    public ResponseEntity<List<VideoResponse>> obtenerVideosPorCurso(@PathVariable Long cursoId) {
        List<VideoResponse> videos = videoService.obtenerVideosPorCurso(cursoId);
        return ResponseEntity.ok(videos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoResponse> obtenerVideoPorId(@PathVariable Long id) {
        VideoResponse video = videoService.obtenerVideoPorId(id);
        return ResponseEntity.ok(video);
    }
}
