package com.otel.server.document.domain;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.index.Indexed;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Searchable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Document
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(builderMethodName = "companyBuilder")
public class Company {
    @Id
    private String id;
    @Searchable
    private String name;
    @Indexed
    private Point location;
    @Indexed
    @Builder.Default
    private Set<String> tags = new HashSet<>();
    @Indexed
    private Integer numberOfEmployees;
    @Indexed
    private Integer yearFounded;
    private String url;
    private boolean publiclyListed;

    public static CompanyBuilder builder(String name, String url, Point location, int numberOfEmployees,
            int yearFounded) {
        return companyBuilder().name(name).url(url).location(location).numberOfEmployees(numberOfEmployees)
                .yearFounded(yearFounded);
    }

}