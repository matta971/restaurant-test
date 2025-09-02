package com.restaurant.service.restaurant.infrastructure.adapter.in.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Test configuration for web layer testing
 * Configures ObjectMapper, formatters, and other web-related beans
 */
@TestConfiguration
public class TestWebConfig implements WebMvcConfigurer {

    /**
     * Configure ObjectMapper for tests with proper Java Time handling
     */
    @Bean
    @Primary
    public ObjectMapper testObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        
        // Configure date/time formatting for consistent JSON serialization
        mapper.findAndRegisterModules();
        
        return mapper;
    }

    /**
     * Add custom formatters for request parameter conversion
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatter(new LocalDateFormatter());
        registry.addFormatter(new LocalTimeFormatter());
    }

    /**
     * Custom LocalDate formatter for URL parameters
     */
    public static class LocalDateFormatter implements org.springframework.format.Formatter<LocalDate> {
        
        @Override
        public LocalDate parse(String text, java.util.Locale locale) throws java.text.ParseException {
            try {
                return LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception e) {
                throw new java.text.ParseException("Unable to parse date: " + text, 0);
            }
        }

        @Override
        public String print(LocalDate date, java.util.Locale locale) {
            return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
    }

    /**
     * Custom LocalTime formatter for URL parameters
     */
    public static class LocalTimeFormatter implements org.springframework.format.Formatter<LocalTime> {
        
        @Override
        public LocalTime parse(String text, java.util.Locale locale) throws java.text.ParseException {
            try {
                return LocalTime.parse(text, DateTimeFormatter.ISO_LOCAL_TIME);
            } catch (Exception e) {
                throw new java.text.ParseException("Unable to parse time: " + text, 0);
            }
        }

        @Override
        public String print(LocalTime time, java.util.Locale locale) {
            return time.format(DateTimeFormatter.ISO_LOCAL_TIME);
        }
    }
}