package org.job.services;

import org.job.services.storage.ZipProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.job.services.storage.ZipService;

@SpringBootApplication
@EnableConfigurationProperties(ZipProperties.class)
public class ZipApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZipApplication.class, args);
    }

    @Bean
    CommandLineRunner init(ZipService storageService) {
        return (args) -> {
            storageService.deleteAll();
            storageService.init();
        };
    }
}