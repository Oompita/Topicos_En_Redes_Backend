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
public class CalificacionResponse {

    private Long id;
    private Long usuarioId;
    private String nombreUsuario;
    private Long cursoId;
    private String cursoTitulo;
    private Integer puntuacion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
}