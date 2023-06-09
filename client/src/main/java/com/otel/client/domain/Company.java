package com.otel.client.domain;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Company(String id, String name, Set<String> tags, Integer numberOfEmployees, Integer yearFounded,
        String url) {
}
