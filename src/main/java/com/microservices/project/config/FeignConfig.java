package com.microservices.project.config;

import com.microservices.project.external.decoder.CustomOrderErrorDecoder;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomOrderErrorDecoder();
    }

}
