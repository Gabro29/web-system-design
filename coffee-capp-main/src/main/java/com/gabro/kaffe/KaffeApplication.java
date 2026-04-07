package com.gabro.kaffe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class KaffeApplication {

    public static void main(String[] args) {
        SpringApplication.run(KaffeApplication.class, args);
    }

}
