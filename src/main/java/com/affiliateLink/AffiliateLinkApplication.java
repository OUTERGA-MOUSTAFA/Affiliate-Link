package com.affiliateLink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AffiliateLinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(AffiliateLinkApplication.class, args);
    }

}