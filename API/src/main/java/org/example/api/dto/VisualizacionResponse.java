package org.example.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisualizacionResponse {
    private Long id;
    private Long videoId;
    private String videoTitulo;
    private Long usuarioId;
    private String nombreUsuario;
    private LocalDateTime fechaVisualizacion;
    private String ipAddress;
}