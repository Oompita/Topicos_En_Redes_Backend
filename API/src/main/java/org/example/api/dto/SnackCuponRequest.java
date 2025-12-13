package org.example.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// ============================================
// REQUEST: Lo que enviamos a Snack
// ============================================
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnackCuponRequest {
    private Long cursoId;
    private String nombreCurso;
    private Long totalVistas;
    private Integer umbralVistas; // 10, 50, 100, 500, 1000
    private String sistemaOrigen; // "UPBmy"
}