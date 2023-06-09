package com.otel.client.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.otel.client.domain.Company;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/call")
@Slf4j
public class ClientController {

    private final String server;
    private final RestTemplate restTemplate;
    private static final String GET_ALL_URL = "/company/all";
    private static final String GET_ONE_URL = "/company?name=";
    private static final String NOT_FOUND_URL = "/company/no";

    public ClientController(RestTemplate restTemplate, @Value("${spring-server.domain}") String server) {
        this.restTemplate = restTemplate;
        this.server = server;
    }

    @GetMapping
    public List<Company> getAllCompany() {
        log.info("get all company from server");
        return restTemplate
                .exchange(server + GET_ALL_URL, HttpMethod.GET, null, new ParameterizedTypeReference<List<Company>>() {
                }).getBody();
    }

    // sample : redis, microsoft
    @GetMapping("/one")
    public Company getOneCompanyDelayReturn(@RequestParam("name") final String name) {
        log.info("get one company from server");
        return restTemplate.exchange(server + GET_ONE_URL + name, HttpMethod.GET, null, Company.class).getBody();
    }

    @GetMapping("/404")
    public String get404() {
        log.info("404 error occur");
        try {
            ResponseEntity<String> response = restTemplate.exchange(server + NOT_FOUND_URL, HttpMethod.GET, null,
                    String.class);
            log.info("Response : {}", response);
            return response.getBody();
        } catch (Exception e) {
            return "not found";
        }
    }
}