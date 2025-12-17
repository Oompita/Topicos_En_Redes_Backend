package org.example.api.upbolisIntegration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request para crear un producto en UPBolis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpbolisProductRequest {
    private String name;
    private String description;
    private Double price;
    private Integer stock;

    @JsonProperty("is_active")
    private Boolean isActive;
}