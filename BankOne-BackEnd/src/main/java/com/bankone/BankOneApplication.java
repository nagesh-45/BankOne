package com.bankone;

import com.bankone.ratelimit.RateLimitProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(RateLimitProperties.class)
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
