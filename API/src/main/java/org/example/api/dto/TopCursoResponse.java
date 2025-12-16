package org.example.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopCursoResponse {
    private String nombreCurso;
    private Long cantidadVistas;
    private Double puntuacion;
}