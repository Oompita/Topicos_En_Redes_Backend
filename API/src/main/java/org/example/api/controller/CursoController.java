package org.example.api.controller;

import lombok.RequiredArgsConstructor;
import org.example.api.dto.CursoRequest;
import org.example.api.dto.CursoResponse;
import org.example.api.service.CursoService;
import org.example.api.service.StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/cursos")
@RequiredArgsConstructor
public class CursoController {

    private final CursoService cursoService;
    private final StorageService storageService;

    @PostMapping
    public ResponseEntity<CursoResponse> crearCurso(
            @RequestParam("titulo") String titulo,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("categoriaId") Long categoriaId,
            @RequestParam(value = "precio", required = false) Double precio,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen) {

        CursoRequest request = new CursoRequest();
        request.setTitulo(titulo);
        request.setDescripcion(descripcion);
        request.setCategoriaId(categoriaId);
        request.setPrecio(precio);

        if (imagen != null && !imagen.isEmpty()) {
            String urlImagen = storageService.guardarImagen(imagen);
            request.setImagenPortada(urlImagen);
        }

        CursoResponse response = cursoService.crearCurso(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CursoResponse> actualizarCurso(
            @PathVariable Long id,
            @RequestParam("titulo") String titulo,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("categoriaId") Long categoriaId,
            @RequestParam(value = "precio", required = false) Double precio,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen) {

        CursoRequest request = new CursoRequest();
        request.setTitulo(titulo);
        request.setDescripcion(descripcion);
        request.setCategoriaId(categoriaId);
        request.setPrecio(precio);

        if (imagen != null && !imagen.isEmpty()) {
            String urlImagen = storageService.guardarImagen(imagen);
            request.setImagenPortada(urlImagen);
        }

        CursoResponse response = cursoService.actualizarCurso(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCurso(@PathVariable Long id) {
        cursoService.eliminarCurso(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publicar")
    public ResponseEntity<CursoResponse> publicarCurso(@PathVariable Long id) {
        CursoResponse response = cursoService.publicarCurso(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/publicos")
    public ResponseEntity<List<CursoResponse>> obtenerCursosPublicos() {
        List<CursoResponse> cursos = cursoService.obtenerCursosPublicos();
        return ResponseEntity.ok(cursos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CursoResponse> obtenerCursoPorId(@PathVariable Long id) {
        CursoResponse curso = cursoService.obtenerCursoPorId(id);
        return ResponseEntity.ok(curso);
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<CursoResponse>> obtenerCursosPorCategoria(@PathVariable Long categoriaId) {
        List<CursoResponse> cursos = cursoService.obtenerCursosPorCategoria(categoriaId);
        return ResponseEntity.ok(cursos);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<CursoResponse>> buscarCursos(
            @RequestParam String q,
            @RequestParam(required = false) Long categoria) {
        List<CursoResponse> cursos = cursoService.buscarCursos(q, categoria);
        return ResponseEntity.ok(cursos);
    }

    @GetMapping("/mis-cursos")
    public ResponseEntity<List<CursoResponse>> obtenerMisCursos() {
        List<CursoResponse> cursos = cursoService.obtenerMisCursos();
        return ResponseEntity.ok(cursos);
    }
}