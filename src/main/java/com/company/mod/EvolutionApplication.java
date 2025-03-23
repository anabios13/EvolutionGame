package com.company.mod;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class EvolutionApplication {
    public static void main(String[] args) {
        SpringApplication.run(EvolutionApplication.class, args);
    }
}