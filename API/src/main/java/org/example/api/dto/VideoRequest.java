package org.example.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VideoRequest {
    
    @NotBlank(message = "El título es requerido")
    @Size(min = 3, max = 200, message = "El título debe tener entre 3 y 200 caracteres")
    private String titulo;
    
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String descripcion;
    
    @NotNull(message = "El orden es requerido")
    @Min(value = 1, message = "El orden debe ser al menos 1")
    private Integer orden;
    
    private Integer duracionSegundos;
}
