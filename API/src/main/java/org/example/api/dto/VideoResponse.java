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
public class VideoResponse {
    private Long id;
    private String titulo;
    private String descripcion;
    private String urlVideo;
    private Integer numero;
    private String duracion;
    private LocalDateTime fechaSubida;
}
