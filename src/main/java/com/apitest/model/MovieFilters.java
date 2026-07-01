package com.apitest.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MovieFilters {
    private String genre;
    private String rating;
    private Double minPrice;
    private Double maxPrice;
    private Integer page;
    private Integer size;
}