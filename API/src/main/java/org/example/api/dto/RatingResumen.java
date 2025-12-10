package org.example.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingResumen {

    private Long cursoId;
    private Double promedioCalificacion; // Promedio de 1.0 a 5.0
    private Long totalCalificaciones;    // Cantidad de votos

    // Distribución de estrellas (opcional pero útil)
    private Long estrellas5;
    private Long estrellas4;
    private Long estrellas3;
    private Long estrellas2;
    private Long estrellas1;
}