package com.tp1methodesagiles.gestiondessalles;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.tp1methodesagiles.gestiondessalles", "com.example.salles"})
@EntityScan("com.example.salles.entity")
@EnableJpaRepositories("com.example.salles.repository")
public class GestiondessallesApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestiondessallesApplication.class, args);
    }

}
