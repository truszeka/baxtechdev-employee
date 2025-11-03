package com.baxtechdev.employee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Employee Service Spring Boot application.
 * Belépési pont az Employee Service Spring Boot alkalmazáshoz.
 */
@SpringBootApplication
public class EmployeeServiceApplication {

    // Logger capturing application lifecycle events.
    // Naplózó, amely az alkalmazás életciklus eseményeit rögzíti.
    private static final Logger log = LoggerFactory.getLogger(EmployeeServiceApplication.class);

    /**
     * Boots the Spring application.
     * Elindítja a Spring alkalmazást.
     *
     * @param args command line arguments passed to the application / az alkalmazásnak átadott parancssori argumentumok
     */
    public static void main(String[] args) {
        // Log the bootstrapping event before delegating to Spring Boot.
        // Naplózzuk az indítási eseményt, mielőtt átadjuk a vezérlést a Spring Bootnak.
        log.info("Starting Employee Service application");
        SpringApplication.run(EmployeeServiceApplication.class, args);
    }
}
