package com.otel.server.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.otel.server.domain.Company;
import com.otel.server.repository.CompanyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
@Slf4j
public class CompanyController {

    private final CompanyRepository repository;

    @GetMapping("/all")
    public List<Company> getAll() {
        log.info("all data presented");
        return repository.findAll();
    }

    @GetMapping()
    public Optional<Company> getCompanyDelayReturn(@RequestParam("name") final String name)
            throws InterruptedException {
        Thread.sleep(5000);
        return repository.findOneByNameContainsIgnoreCase(name);
    }

}
