package org.example.api.controller;

import lombok.RequiredArgsConstructor;
import org.example.api.dto.CuponResponse;
import org.example.api.dto.MarcarCuponUsadoRequest;
import org.example.api.service.CuponService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cupones")
@RequiredArgsConstructor
public class CuponController {

    private final CuponService cuponService;

    /**
     * Obtener cupón disponible de un curso (si existe)
     * GET /api/cupones/curso/{cursoId}/disponible
     * Público - para mostrar en el detalle del curso
     */
    @GetMapping("/curso/{cursoId}/disponible")
    public ResponseEntity<CuponResponse> obtenerCuponDisponible(@PathVariable Long cursoId) {
        CuponResponse cupon = cuponService.obtenerCuponDisponible(cursoId);

        if (cupon == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(cupon);
    }

    /**
     * Obtener todos los cupones de un curso
     * GET /api/cupones/curso/{cursoId}
     */
    @GetMapping("/curso/{cursoId}")
    public ResponseEntity<List<CuponResponse>> obtenerCuponesPorCurso(@PathVariable Long cursoId) {
        List<CuponResponse> cupones = cuponService.obtenerCuponesPorCurso(cursoId);
        return ResponseEntity.ok(cupones);
    }

    /**
     * Marcar un cupón como usado (cuando el estudiante lo copia)
     * POST /api/cupones/marcar-usado
     * Requiere autenticación
     */
    @PostMapping("/marcar-usado")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> marcarCuponComoUsado(@RequestBody MarcarCuponUsadoRequest request) {
        boolean success = cuponService.marcarCuponComoUsado(request.getCodigoCupon());

        if (success) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.badRequest().build();
    }

    /**
     * ENDPOINT PARA SNACK: Exponer información de cursos con vistas
     * GET /api/cupones/cursos-con-vistas
     * Este endpoint es para que Snack pueda consultarlo si lo necesitan
     */
    @GetMapping("/cursos-con-vistas")
    public ResponseEntity<List<CuponResponse>> obtenerCursosConVistasParaSnack(
            @RequestParam(required = false, defaultValue = "10") Integer umbralMinimo) {

        List<CuponResponse> cupones = cuponService.obtenerCursosConUmbralAlcanzado(umbralMinimo);
        return ResponseEntity.ok(cupones);
    }

    /**
     * Admin: Obtener todos los cupones del sistema
     * GET /api/cupones/admin/todos
     */
    @GetMapping("/admin/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CuponResponse>> obtenerTodosCupones() {
        List<CuponResponse> cupones = cuponService.obtenerTodosCupones();
        return ResponseEntity.ok(cupones);
    }
}