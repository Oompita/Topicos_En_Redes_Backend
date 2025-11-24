package org.example.api.service;

import lombok.RequiredArgsConstructor;
import org.example.api.exception.BadRequestException;
import org.example.api.exception.ResourceNotFoundException;
import org.example.api.model.Categoria;
import org.example.api.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Transactional
    public Categoria crearCategoria(String nombre, String descripcion) {
        if (categoriaRepository.existsByNombre(nombre)) {
            throw new BadRequestException("La categoría ya existe");
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setDescripcion(descripcion);

        return categoriaRepository.save(categoria);
    }

    public List<Categoria> obtenerTodasCategorias() {
        return categoriaRepository.findAll();
    }

    public Categoria obtenerCategoriaPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
    }

    @Transactional
    public Categoria actualizarCategoria(Long id, String nombre, String descripcion) {
        Categoria categoria = obtenerCategoriaPorId(id);

        if (!categoria.getNombre().equals(nombre) && categoriaRepository.existsByNombre(nombre)) {
            throw new BadRequestException("Ya existe una categoría con ese nombre");
        }

        categoria.setNombre(nombre);
        categoria.setDescripcion(descripcion);

        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void eliminarCategoria(Long id) {
        Categoria categoria = obtenerCategoriaPorId(id);

        if (categoria.getCursos() != null && !categoria.getCursos().isEmpty()) {
            throw new BadRequestException("No se puede eliminar una categoría con cursos asociados");
        }

        categoriaRepository.delete(categoria);
    }
}
