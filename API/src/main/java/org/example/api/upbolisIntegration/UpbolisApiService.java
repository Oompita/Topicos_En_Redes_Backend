package org.example.api.upbolisIntegration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpbolisApiService {

    private final RestTemplate restTemplate;

    @Value("${upbolis.api.base-url}")
    private String upbolisApiBaseUrl;

    @Value("${upbolis.api.username}")
    private String upbolisUsername;

    @Value("${upbolis.api.password}")
    private String upbolisPassword;

    private String cachedToken = null;

    private String obtenerToken() {
        try {
            log.info("Obteniendo token JWT de UPBolis API...");

            UpbolisLoginRequest loginRequest = new UpbolisLoginRequest(upbolisUsername, upbolisPassword);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<UpbolisLoginRequest> request = new HttpEntity<>(loginRequest, headers);

            ResponseEntity<UpbolisLoginResponse> response = restTemplate.postForEntity(
                    upbolisApiBaseUrl + "/auth/login",
                    request,
                    UpbolisLoginResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                cachedToken = response.getBody().getToken();
                log.info("Token JWT de UPBolis obtenido exitosamente");
                return cachedToken;
            } else {
                throw new RuntimeException("Error al obtener token de UPBolis");
            }
        } catch (Exception e) {
            log.error("Error al autenticar con UPBolis: {}", e.getMessage());
            throw new RuntimeException("Error al autenticar con UPBolis", e);
        }
    }

    /**
     * Crea un producto (curso) en UPBolis
     * POST /seller/products
     */
    public UpbolisProductResponse crearProducto(String nombre, String descripcion, Double precio) {
        try {
            log.info("Creando producto en UPBolis - Nombre: {}, Precio: {}", nombre, precio);

            if (cachedToken == null) {
                obtenerToken();
            }

            UpbolisProductRequest productRequest = UpbolisProductRequest.builder()
                    .name(nombre)
                    .description(descripcion != null ? descripcion : "Curso educativo de UPBmy")
                    .price(precio != null ? precio : 0.0)
                    .stock(999)
                    .isActive(true)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(cachedToken);

            HttpEntity<UpbolisProductRequest> request = new HttpEntity<>(productRequest, headers);

            ResponseEntity<UpbolisProductResponse> response = restTemplate.postForEntity(
                    upbolisApiBaseUrl + "/seller/products",
                    request,
                    UpbolisProductResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                log.info("Producto creado en UPBolis con ID: {}", response.getBody().getId());
                return response.getBody();
            } else if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                cachedToken = null;
                obtenerToken();
                return crearProducto(nombre, descripcion, precio);
            } else {
                throw new RuntimeException("Error al crear producto en UPBolis");
            }

        } catch (Exception e) {
            log.error("Error al crear producto en UPBolis: {}", e.getMessage());
            return null;
        }
    }

    /**
     * LIMITACIÓN DE UPBOLIS: Solo se puede actualizar stock e is_active
     * El precio NO se puede actualizar una vez creado el producto
     */
    public UpbolisProductResponse actualizarEstadoProducto(Long upbolisProductId, Boolean activo) {
        try {
            log.info("Actualizando estado del producto {} en UPBolis a: {}",
                    upbolisProductId, activo ? "ACTIVO" : "INACTIVO");

            if (cachedToken == null) {
                obtenerToken();
            }

            // Según el código de UPBolis, solo podemos enviar estos campos
            UpbolisProductUpdateRequest updateRequest = UpbolisProductUpdateRequest.builder()
                    .stock(activo ? 999 : 0) // Stock alto si activo, 0 si inactivo
                    .isActive(activo)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(cachedToken);

            HttpEntity<UpbolisProductUpdateRequest> request = new HttpEntity<>(updateRequest, headers);

            ResponseEntity<UpbolisProductResponse> response = restTemplate.exchange(
                    upbolisApiBaseUrl + "/seller/products/" + upbolisProductId,
                    HttpMethod.DELETE,
                    request,
                    UpbolisProductResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Estado del producto actualizado en UPBolis");
                return response.getBody();
            } else if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                cachedToken = null;
                obtenerToken();
                return actualizarEstadoProducto(upbolisProductId, activo);
            } else {
                throw new RuntimeException("Error al actualizar estado en UPBolis");
            }

        } catch (Exception e) {
            log.error("Error al actualizar estado en UPBolis: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Ya que UPBolis no permite actualizar precio, desactivamos el viejo y creamos uno nuevo
     */
    public UpbolisProductResponse recrearProductoConNuevoPrecio(
            Long upbolisProductIdViejo,
            String nombre,
            String descripcion,
            Double nuevoPrecio) {

        try {
            log.warn("⚠UPBolis no permite actualizar precio. Desactivando producto viejo y creando uno nuevo...");

            // 1. Desactivar producto viejo
            if (upbolisProductIdViejo != null) {
                actualizarEstadoProducto(upbolisProductIdViejo, false);
                log.info("Producto viejo {} desactivado", upbolisProductIdViejo);
            }

            // 2. Crear nuevo producto con el nuevo precio
            UpbolisProductResponse nuevoProducto = crearProducto(nombre, descripcion, nuevoPrecio);

            if (nuevoProducto != null) {
                log.info("Nuevo producto creado con ID: {} y precio: {}",
                        nuevoProducto.getId(), nuevoPrecio);
            }

            return nuevoProducto;

        } catch (Exception e) {
            log.error("Error al recrear producto: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Desactiva un producto en UPBolis
     */
    public boolean desactivarProducto(Long upbolisProductId) {
        try {
            log.info("Desactivando producto {} en UPBolis", upbolisProductId);
            UpbolisProductResponse response = actualizarEstadoProducto(upbolisProductId, false);
            return response != null;
        } catch (Exception e) {
            log.error("Error al desactivar producto: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Activa un producto en UPBolis
     */
    public boolean activarProducto(Long upbolisProductId) {
        try {
            log.info("Activando producto {} en UPBolis", upbolisProductId);
            UpbolisProductResponse response = actualizarEstadoProducto(upbolisProductId, true);
            return response != null;
        } catch (Exception e) {
            log.error("Error al activar producto: {}", e.getMessage());
            return false;
        }
    }

    public boolean verificarConectividad() {
        try {
            obtenerToken();
            return true;
        } catch (Exception e) {
            log.error("Error al verificar conectividad: {}", e.getMessage());
            return false;
        }
    }
}