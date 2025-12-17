package org.example.api.upbolisIntegration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request para actualizar un producto en UPBolis
 * PUT /seller/products/{product}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpbolisProductUpdateRequest {
    private String name;
    private int price;
    private Integer stock;

    @JsonProperty("is_active")
    private Boolean isActive;
}
