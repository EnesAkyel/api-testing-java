package com.apitest.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    private int mid;
    private String name;
    private String genre;
    private double price;
    private String rating;
    private int studio;
}