package com.example.smartmart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String tokenType = "Bearer";
    private String accessToken;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private Set<String> roles;
}
