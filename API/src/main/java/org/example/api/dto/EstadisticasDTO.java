package org.example.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasDTO {
    private Long totalUsuarios;
    private Long estudiantes;
    private Long instructores;
    private Long totalCursos;
    private Long cursosPublicados;
    private Long cursosBorrador;
    private Long totalVideos;
    private Long totalCategorias;
    private Long totalCalificaciones;
}
