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
public class CuponResponse {
    private Long id;
    private Long cursoId;
    private String nombreCurso;
    private String codigoCupon;
    private Integer vistasRequeridas;
    private Integer porcentajeDescuento;
    private Boolean activo;
    private Boolean usado;
    private Boolean vigente;
    private LocalDateTime fechaGeneracion;
    private LocalDateTime fechaExpiracion;
    private LocalDateTime fechaUso;
}
