package com.zoom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ZoomBackendApplication {

    private static final Logger logger = LoggerFactory.getLogger(ZoomBackendApplication.class);
    private static final String VERSION = "1.0.1";

    public static void main(String[] args) {
        logger.info("\n" +
                "===========================================\n" +
                "   🚀 Zoom Backend - Version " + VERSION + "\n" +
                "===========================================\n");
        SpringApplication.run(ZoomBackendApplication.class, args);
    }
}
