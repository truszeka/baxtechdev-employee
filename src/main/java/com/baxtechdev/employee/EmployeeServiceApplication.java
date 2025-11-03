package com.baxtechdev.employee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Employee Service Spring Boot application.
 */
@SpringBootApplication
public class EmployeeServiceApplication {

    /**
     * Boots the Spring application.
     *
     * @param args command line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(EmployeeServiceApplication.class, args);
    }
}
