package org.example.api.repository;

import org.example.api.model.Categoria;
import org.example.api.model.Curso;
import org.example.api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {
    List<Curso> findByPublicadoTrue();
    List<Curso> findByInstructor(Usuario instructor);
    List<Curso> findByCategoria(Categoria categoria);
    List<Curso> findByCategoriaAndPublicadoTrue(Categoria categoria);

    @Query("SELECT c FROM Curso c WHERE c.publicado = true AND " +
            "(LOWER(c.titulo) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.descripcion) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Curso> buscarCursos(@Param("keyword") String keyword);

    @Query("SELECT c FROM Curso c WHERE c.publicado = true AND c.categoria.id = :categoriaId AND " +
            "(LOWER(c.titulo) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.descripcion) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Curso> buscarCursosPorCategoria(@Param("keyword") String keyword, @Param("categoriaId") Long categoriaId);
}
