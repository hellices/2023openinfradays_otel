package com.otel.server.domain;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private Set<String> tags = new HashSet<>();
    private Integer numberOfEmployees;
    private Integer yearFounded;
    private String url;
    private boolean publiclyListed;

    @Builder
    public Company(String name, String url, int numberOfEmployees, int yearFounded) {
        this.name = name;
        this.url = url;
        this.numberOfEmployees = numberOfEmployees;
        this.yearFounded = yearFounded;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

}