package org.example.api.snackIntegration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SnackLoginRequest {
    private String username;
    private String password;
}