package org.example.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.api.dto.CursoResponse;
import org.example.api.dto.UsuarioRequest;
import org.example.api.dto.UsuarioResponse;
import org.example.api.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ==================== ESTADÍSTICAS ====================

    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        Map<String, Object> stats = adminService.obtenerEstadisticas();
        return ResponseEntity.ok(stats);
    }

    // ==================== GESTIÓN DE USUARIOS ====================

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioResponse>> obtenerTodosLosUsuarios() {
        List<UsuarioResponse> usuarios = adminService.obtenerTodosLosUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioResponse> obtenerUsuarioPorId(@PathVariable Long id) {
        UsuarioResponse usuario = adminService.obtenerUsuarioPorId(id);
        return ResponseEntity.ok(usuario);
    }

    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioResponse> crearUsuario(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse usuario = adminService.crearUsuario(request);
        return new ResponseEntity<>(usuario, HttpStatus.CREATED);
    }

    @PutMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioResponse> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse usuario = adminService.actualizarUsuario(id, request);
        return ResponseEntity.ok(usuario);
    }

    @PatchMapping("/usuarios/{id}/estado")
    public ResponseEntity<Void> cambiarEstadoUsuario(
            @PathVariable Long id,
            @RequestParam Boolean activo) {
        adminService.cambiarEstadoUsuario(id, activo);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        adminService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== GESTIÓN DE CURSOS ====================

    @GetMapping("/cursos")
    public ResponseEntity<List<CursoResponse>> obtenerTodosLosCursos() {
        List<CursoResponse> cursos = adminService.obtenerTodosLosCursos();
        return ResponseEntity.ok(cursos);
    }

    @DeleteMapping("/cursos/{id}")
    public ResponseEntity<Void> eliminarCurso(@PathVariable Long id) {
        adminService.eliminarCursoAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/cursos/{id}/estado")
    public ResponseEntity<CursoResponse> cambiarEstadoCurso(
            @PathVariable Long id,
            @RequestParam Boolean publicado) {
        CursoResponse curso = adminService.cambiarEstadoCurso(id, publicado);
        return ResponseEntity.ok(curso);
    }

    // ==================== GESTIÓN DE VIDEOS ====================

    @DeleteMapping("/videos/{id}")
    public ResponseEntity<Void> eliminarVideo(@PathVariable Long id) {
        adminService.eliminarVideoAdmin(id);
        return ResponseEntity.noContent().build();
    }
}