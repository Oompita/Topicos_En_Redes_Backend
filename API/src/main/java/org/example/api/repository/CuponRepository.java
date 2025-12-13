package org.example.api.repository;

import org.example.api.model.Cupon;
import org.example.api.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuponRepository extends JpaRepository<Cupon, Long> {

    // Buscar cupones de un curso
    List<Cupon> findByCursoId(Long cursoId);

    // Buscar cupón activo y disponible de un curso
    @Query("SELECT c FROM Cupon c WHERE c.curso.id = :cursoId AND c.activo = true AND c.usado = false ORDER BY c.fechaGeneracion DESC")
    Optional<Cupon> findCuponDisponibleByCurso(@Param("cursoId") Long cursoId);

    // Verificar si ya existe cupón para un umbral específico
    boolean existsByCursoAndVistasRequeridas(Curso curso, Integer vistasRequeridas);

    // Buscar por código
    Optional<Cupon> findByCodigoCupon(String codigoCupon);

    // Contar cupones activos
    long countByActivoTrueAndUsadoFalse();

    // Cupones usados
    List<Cupon> findByUsadoTrue();
}