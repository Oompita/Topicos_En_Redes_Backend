package org.example.api.repository;

import org.example.api.model.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {

    // Buscar calificación específica de un usuario para un curso
    Optional<Calificacion> findByUsuarioIdAndCursoId(Long usuarioId, Long cursoId);

    // Verificar si existe calificación
    boolean existsByUsuarioIdAndCursoId(Long usuarioId, Long cursoId);

    // Todas las calificaciones de un curso
    List<Calificacion> findByCursoId(Long cursoId);

    // Promedio de calificación de un curso
    @Query("SELECT AVG(c.puntuacion) FROM Calificacion c WHERE c.curso.id = :cursoId")
    Double obtenerPromedioCalificacion(@Param("cursoId") Long cursoId);

    // Total de calificaciones de un curso
    Long countByCursoId(Long cursoId);

    // Contar por puntuación (para distribución)
    @Query("SELECT COUNT(c) FROM Calificacion c WHERE c.curso.id = :cursoId AND c.puntuacion = :puntuacion")
    Long countByCursoIdAndPuntuacion(@Param("cursoId") Long cursoId, @Param("puntuacion") Integer puntuacion);

    List<Calificacion> findByUsuarioId(Long usuarioId);

    @Query("SELECT c FROM Calificacion c WHERE " +
            "(:cursoId IS NULL OR c.curso.id = :cursoId) AND " +
            "(:usuarioId IS NULL OR c.usuario.id = :usuarioId) AND " +
            "(:fechaDesde IS NULL OR c.fechaModificacion >= :fechaDesde OR (c.fechaModificacion IS NULL AND c.fechaCreacion >= :fechaDesde)) AND " +
            "(:fechaHasta IS NULL OR c.fechaModificacion <= :fechaHasta OR (c.fechaModificacion IS NULL AND c.fechaCreacion <= :fechaHasta)) " +
            "ORDER BY COALESCE(c.fechaModificacion, c.fechaCreacion) DESC")
    List<Calificacion> buscarConFiltros(
            @Param("cursoId") Long cursoId,
            @Param("usuarioId") Long usuarioId,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta
    );
}