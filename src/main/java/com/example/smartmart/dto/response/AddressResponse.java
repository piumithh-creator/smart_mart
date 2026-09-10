package com.example.smartmart.dto.response;

import com.example.smartmart.enumiration.AddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {
    private Long id;
    private AddressType addressType;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private boolean defaultAddress;
    private LocalDateTime createdAt;
}
