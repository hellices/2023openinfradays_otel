package com.otel.server.config;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.geo.Point;

import com.otel.server.document.domain.Company;
import com.otel.server.document.repository.CompanyRepository;
import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;

@Configuration
@EnableRedisDocumentRepositories(basePackages = "com.otel.server.document.*")
public class ServerConfig {

    @Bean
    CommandLineRunner loadTestData(CompanyRepository companyRepo) {
        return args -> {
            // remove all companies
            companyRepo.deleteAll();

            // Create a couple of `Company` domain entities
            Company redis = Company.builder(
                    "Redis", "https://redis.com", new Point(-122.066540, 37.377690), 526, 2011 //
            ).build();
            redis.setTags(Set.of("fast", "scalable", "reliable"));

            Company microsoft = Company.builder(
                    "Microsoft", "https://microsoft.com", new Point(-122.124500, 47.640160), 182268, 1975 //
            ).build();
            microsoft.setTags(Set.of("innovative", "reliable"));

            // save companies to the database
            companyRepo.save(redis);
            companyRepo.save(microsoft);
        };
    }
}
