package com.cine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CineApplication {
    public static void main(String[] args) {
        SpringApplication.run(CineApplication.class, args);
    }
}

