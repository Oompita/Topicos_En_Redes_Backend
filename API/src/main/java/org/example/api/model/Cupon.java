package org.example.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cupones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @Column(name = "codigo_cupon", nullable = false, unique = true)
    private String codigoCupon;

    @Column(name = "vistas_requeridas", nullable = false)
    private Integer vistasRequeridas; // 10, 50, 100, 500, 1000

    @Column(name = "porcentaje_descuento")
    private Integer porcentajeDescuento; // Ej: 10, 15, 20

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private Boolean usado = false;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;

    @Column(name = "usuario_uso_id")
    private Long usuarioUsoId; // ID del usuario que usó el cupón

    @Column(name = "fecha_uso")
    private LocalDateTime fechaUso;

    @PrePersist
    protected void onCreate() {
        fechaGeneracion = LocalDateTime.now();
    }

    /**
     * Marcar el cupón como usado
     */
    public void marcarComoUsado(Long usuarioId) {
        this.usado = true;
        this.usuarioUsoId = usuarioId;
        this.fechaUso = LocalDateTime.now();
    }

    /**
     * Verificar si el cupón está vigente
     */
    @Transient
    public boolean isVigente() {
        if (!activo || usado) {
            return false;
        }
        if (fechaExpiracion != null) {
            return LocalDateTime.now().isBefore(fechaExpiracion);
        }
        return true;
    }
}