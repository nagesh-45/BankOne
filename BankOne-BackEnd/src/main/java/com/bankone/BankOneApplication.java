package com.bankone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BankOneApplication {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(BankOneApplication.class);

    public static void main(String[] args) {
        LOGGER.info("Starting BankOne Application...");
        SpringApplication.run(BankOneApplication.class, args);
        LOGGER.info("BankOne Application Started Successfully.");
    }

}
