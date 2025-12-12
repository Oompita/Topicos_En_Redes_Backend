package org.example.api.repository;

import org.example.api.model.Visualizacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VisualizacionRepository extends JpaRepository<Visualizacion, Long> {

    // Contar vistas totales de un video
    Long countByVideoId(Long videoId);

    // Obtener todas las visualizaciones de un video
    List<Visualizacion> findByVideoId(Long videoId);

    // Para futuro: historial del usuario
    List<Visualizacion> findByUsuarioIdOrderByFechaVisualizacionDesc(Long usuarioId);

    // Contar vistas de un usuario
    Long countByUsuarioId(Long usuarioId);

    // Contar vistas totales de todos los videos de un curso
    @Query("SELECT COUNT(v) FROM Visualizacion v WHERE v.video.curso.id = :cursoId")
    Long countByCursoId(@Param("cursoId") Long cursoId);

    // Para admin: buscar con filtros
    @Query("SELECT v FROM Visualizacion v WHERE " +
            "(:videoId IS NULL OR v.video.id = :videoId) AND " +
            "(:usuarioId IS NULL OR v.usuario.id = :usuarioId) AND " +
            "(:fechaDesde IS NULL OR v.fechaVisualizacion >= :fechaDesde) AND " +
            "(:fechaHasta IS NULL OR v.fechaVisualizacion <= :fechaHasta) " +
            "ORDER BY v.fechaVisualizacion DESC")
    List<Visualizacion> buscarConFiltros(
            @Param("videoId") Long videoId,
            @Param("usuarioId") Long usuarioId,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta
    );

    // Estadísticas generales
    @Query("SELECT COUNT(v) FROM Visualizacion v")
    Long contarTotalVisualizaciones();
}