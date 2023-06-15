package com.otel.server.config;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.otel.server.domain.Company;
import com.otel.server.repository.CompanyRepository;

@Configuration
public class ServerConfig {

    @Bean
    CommandLineRunner loadTestData(CompanyRepository companyRepo) {
        return args -> {
            // remove all companies
            companyRepo.deleteAll();

            // Create a couple of `Company` domain entities
            Company google = Company.builder().name("Google").url("https://google.com").numberOfEmployees(156500)
                    .yearFounded(1998).build();
            google.setTags(Set.of("accessible", "easier", "useful"));

            Company microsoft = Company.builder().name("Microsoft").url("https://microsoft.com")
                    .numberOfEmployees(182268).yearFounded(1975).build();
            microsoft.setTags(Set.of("innovative", "reliable"));

            Company amazon = Company.builder().name("Samsung Electronics").url("https://samsung.com")
                    .numberOfEmployees(113485).yearFounded(1988).build();

            Company shinhancard = Company.builder().name("Shinhancard").url("https://shinhancard.com")
                    .numberOfEmployees(3062).yearFounded(2007).build();

            // save companies to the database
            companyRepo.save(google);
            companyRepo.save(microsoft);
            companyRepo.save(amazon);
            companyRepo.save(shinhancard);
        };
    }
}
