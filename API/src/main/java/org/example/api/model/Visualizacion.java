package org.example.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "visualizaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Visualizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = true) // Nullable para vistas anónimas
    private Usuario usuario;

    @Column(name = "fecha_visualizacion", nullable = false)
    private LocalDateTime fechaVisualizacion;

    @Column(name = "ip_address")
    private String ipAddress; // Por si queremos trackear IPs en el futuro

    @PrePersist
    protected void onCreate() {
        fechaVisualizacion = LocalDateTime.now();
    }
}