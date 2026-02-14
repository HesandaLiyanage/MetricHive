package com.hess.metrichive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MetricHiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetricHiveApplication.class, args);
    }

}
