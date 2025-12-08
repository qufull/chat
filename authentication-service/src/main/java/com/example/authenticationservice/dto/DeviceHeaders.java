package com.example.authenticationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestHeader;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class DeviceHeaders {

    private String deviceId;
    private String timestamp;
    private String signature;

}
