package org.example.api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint que Snack consulta para validar las 10 vistas
 * Snack llamará GET /api/snack/views-validation cuando necesite verificar
 */
@RestController
@RequestMapping("/api/snack")
@RequiredArgsConstructor
@Slf4j
public class SnackIntegrationController {

    /**
     * Endpoint que retorna 10 para que Snack pueda generar código
     * Este endpoint es consultado por ExternalIntClient de Snack
     */
    @GetMapping("/views-validation")
    public ResponseEntity<Integer> getViewsValidation() {
        log.info("Snack consultando validación de vistas");
        return ResponseEntity.ok(10);
    }
}