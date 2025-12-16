package org.example.api.controller;

import lombok.RequiredArgsConstructor;
import org.example.api.dto.TopCursoResponse;
import org.example.api.service.CursoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/external")
@RequiredArgsConstructor
public class ExternalApiController {

    private final CursoService cursoService;

    @GetMapping("/cursos/top-vistas")
    public ResponseEntity<List<TopCursoResponse>> getTop3CursosConMasVistas() {
        List<TopCursoResponse> topCursos = cursoService.getTop3CursosConMasVistas();
        return ResponseEntity.ok(topCursos);
    }
}