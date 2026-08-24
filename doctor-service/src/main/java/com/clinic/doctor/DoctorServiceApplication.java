package com.clinic.doctor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class DoctorServiceApplication {
    public static void main(String[] args) { SpringApplication.run(DoctorServiceApplication.class, args); }
    @Bean RestClient.Builder restClientBuilder() { return RestClient.builder(); }
}
