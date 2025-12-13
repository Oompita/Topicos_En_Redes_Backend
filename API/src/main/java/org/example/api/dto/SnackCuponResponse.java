package org.example.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// ============================================
// RESPONSE: Lo que recibimos de Snack
// ============================================
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnackCuponResponse {
    private String codigoCupon;
    private Integer porcentajeDescuento;
    private LocalDateTime fechaExpiracion;
    private String mensaje;
    private Boolean success;
}