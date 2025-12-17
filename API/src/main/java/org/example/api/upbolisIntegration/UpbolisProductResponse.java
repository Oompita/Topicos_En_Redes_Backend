package org.example.api.upbolisIntegration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response de UPBolis al crear/actualizar un producto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpbolisProductResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer stock;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("seller_id")
    private Long sellerId;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;
}
