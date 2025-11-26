package org.example.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "videos",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"curso_id", "orden"},
                name = "uk_curso_orden"
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(length = 1000)
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @Column(name = "url_video", nullable = false)
    private String urlVideo;

    @Column(nullable = false)
    private Integer orden;

    @Column(name = "duracion_segundos")
    private Integer duracionSegundos;

    @Column(name = "fecha_subida", nullable = false)
    private LocalDateTime fechaSubida;

    @PrePersist
    protected void onCreate() {
        fechaSubida = LocalDateTime.now();
    }

    @Transient
    public String getDuracionFormateada() {
        if (duracionSegundos == null) {
            return "0:00";
        }
        int minutos = duracionSegundos / 60;
        int segundos = duracionSegundos % 60;
        return String.format("%d:%02d", minutos, segundos);
    }

    @Transient
    public int getDuracionMinutos() {
        return duracionSegundos != null ? (int) Math.ceil(duracionSegundos / 60.0) : 0;
    }
}
