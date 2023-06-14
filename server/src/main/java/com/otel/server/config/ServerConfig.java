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
            Company redis = Company.builder().name("Redis").url("https://redis.com").numberOfEmployees(526)
                    .yearFounded(2011).build();
            redis.setTags(Set.of("fast", "scalable", "reliable"));

            Company microsoft = Company.builder().name("Microsoft").url("https://microsoft.com")
                    .numberOfEmployees(182268).yearFounded(1975).build();
            microsoft.setTags(Set.of("innovative", "reliable"));

            // save companies to the database
            companyRepo.save(redis);
            companyRepo.save(microsoft);
        };
    }
}
