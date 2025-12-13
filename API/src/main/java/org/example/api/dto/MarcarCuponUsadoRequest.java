package org.example.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// ============================================
// REQUEST para marcar cupón como usado
// ============================================
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarcarCuponUsadoRequest {
    private String codigoCupon;
}
