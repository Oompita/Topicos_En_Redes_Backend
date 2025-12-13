package org.example.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.api.dto.CuponResponse;
import org.example.api.model.Cupon;
import org.example.api.model.Usuario;
import org.example.api.repository.CuponRepository;
import org.example.api.repository.VisualizacionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CuponService {

    private final CuponRepository cuponRepository;
    private final VisualizacionRepository visualizacionRepository;
    private final SnackIntegrationService snackIntegrationService;

    /**
     * Obtener cupón disponible de un curso (si existe)
     */
    public CuponResponse obtenerCuponDisponible(Long cursoId) {
        return cuponRepository.findCuponDisponibleByCurso(cursoId)
                .map(this::convertirACuponResponse)
                .orElse(null);
    }

    /**
     * Obtener todos los cupones de un curso
     */
    public List<CuponResponse> obtenerCuponesPorCurso(Long cursoId) {
        return cuponRepository.findByCursoId(cursoId).stream()
                .map(this::convertirACuponResponse)
                .collect(Collectors.toList());
    }

    /**
     * Marcar cupón como usado cuando un estudiante lo copia
     */
    @Transactional
    public boolean marcarCuponComoUsado(String codigoCupon) {
        try {
            Usuario usuario = getUsuarioAutenticado();
            return snackIntegrationService.marcarCuponComoUsado(codigoCupon, usuario.getId());
        } catch (Exception e) {
            log.error("Error al marcar cupón como usado: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtener cursos que alcanzaron cierto umbral de vistas
     * Este endpoint puede ser consumido por Snack
     */
    public List<CuponResponse> obtenerCursosConUmbralAlcanzado(Integer umbralMinimo) {
        return cuponRepository.findAll().stream()
                .filter(c -> c.getVistasRequeridas() >= umbralMinimo)
                .map(this::convertirACuponResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener todos los cupones (Admin)
     */
    public List<CuponResponse> obtenerTodosCupones() {
        return cuponRepository.findAll().stream()
                .map(this::convertirACuponResponse)
                .collect(Collectors.toList());
    }

    private Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Usuario) {
            return (Usuario) authentication.getPrincipal();
        }
        return null;
    }

    private CuponResponse convertirACuponResponse(Cupon cupon) {
        return CuponResponse.builder()
                .id(cupon.getId())
                .cursoId(cupon.getCurso().getId())
                .nombreCurso(cupon.getCurso().getTitulo())
                .codigoCupon(cupon.getCodigoCupon())
                .vistasRequeridas(cupon.getVistasRequeridas())
                .porcentajeDescuento(cupon.getPorcentajeDescuento())
                .activo(cupon.getActivo())
                .usado(cupon.getUsado())
                .vigente(cupon.isVigente())
                .fechaGeneracion(cupon.getFechaGeneracion())
                .fechaExpiracion(cupon.getFechaExpiracion())
                .fechaUso(cupon.getFechaUso())
                .build();
    }
}