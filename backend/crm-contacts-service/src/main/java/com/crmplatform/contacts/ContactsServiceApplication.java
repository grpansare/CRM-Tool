package com.crmplatform.contacts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"com.crmplatform"})
@EnableDiscoveryClient
public class ContactsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContactsServiceApplication.class, args);
    }
} 