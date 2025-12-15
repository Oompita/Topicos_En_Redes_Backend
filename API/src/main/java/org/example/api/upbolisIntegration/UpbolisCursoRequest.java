package org.example.api.upbolisIntegration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para enviar información de curso a la API de UPBolis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpbolisCursoRequest {
    private Long cursoId;
    private String nombre;
    private String descripcion;
    private Double precio;
}