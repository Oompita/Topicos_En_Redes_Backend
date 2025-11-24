package org.example.api.repository;

import org.example.api.model.Curso;
import org.example.api.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByCursoOrderByOrdenAsc(Curso curso);
    List<Video> findByCursoIdOrderByOrdenAsc(Long cursoId);
}