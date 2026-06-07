package com.police.eom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EomApplication {
    public static void main(String[] args) {
        SpringApplication.run(EomApplication.class, args);
    }
}
