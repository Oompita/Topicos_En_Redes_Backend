package org.example.api.upbolisIntegration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta cuando se registra un curso en UPBolis
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpbolisCursoResponse {
    private String message;
    private Long cursoId;
    private String upbolisProductId;
    private String status;
}