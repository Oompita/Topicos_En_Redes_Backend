package org.example.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CursoResponse {
    private Long id;
    private String titulo;
    private String descripcion;
    private String instructor;
    private Long instructorId;
    private String categoria;
    private Long categoriaId;
    private String imagenPortada;
    private LocalDateTime fechaCreacion;
    private Boolean publicado;
    private Integer videos;
    private String duracion;
    private List<VideoResponse> listaVideos;
    private Long totalVistas;
    private Double precio;
}