package com.restaurant.service.reservation.infrastructure.adapter.out.client.config;

import com.restaurant.service.reservation.domain.port.out.RestaurantServiceClient;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Custom error decoder for Feign client calls to Restaurant Service
 */
@Component
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());
        String url = response.request().url();
        
        log.error("Feign client error: {} {} - Status: {}", 
                response.request().httpMethod(), url, status);

        switch (status) {
            case NOT_FOUND:
                if (methodKey.contains("getRestaurant")) {
                    return new RestaurantServiceClient.RestaurantNotFoundException(
                            extractRestaurantIdFromUrl(url));
                }
                break;
                
            case BAD_REQUEST:
                return new IllegalArgumentException("Invalid request to Restaurant Service: " + url);
                
            case INTERNAL_SERVER_ERROR:
            case BAD_GATEWAY:
            case SERVICE_UNAVAILABLE:
            case GATEWAY_TIMEOUT:
                return new RestaurantServiceClient.RestaurantServiceException(
                        "Restaurant Service is temporarily unavailable", 
                        new RuntimeException("HTTP " + status));
                        
            default:
                break;
        }

        return defaultErrorDecoder.decode(methodKey, response);
    }

    private Long extractRestaurantIdFromUrl(String url) {
        try {
            // Extract restaurant ID from URL like /api/restaurants/{id}
            String[] parts = url.split("/");
            for (int i = 0; i < parts.length - 1; i++) {
                if ("restaurants".equals(parts[i])) {
                    return Long.parseLong(parts[i + 1]);
                }
            }
        } catch (Exception e) {
            log.warn("Could not extract restaurant ID from URL: {}", url);
        }
        return null;
    }
}