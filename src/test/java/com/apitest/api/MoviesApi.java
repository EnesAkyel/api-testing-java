package com.apitest.api;

import com.apitest.model.Movie;
import com.apitest.model.MovieFilters;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class MoviesApi {

    public Response getMovies() {
        return given().get("/movies");
    }

    public Response getMovies(MovieFilters filters) {
        var req = given();
        if (filters.getGenre() != null)    req = req.queryParam("genre", filters.getGenre());
        if (filters.getRating() != null)   req = req.queryParam("rating", filters.getRating());
        if (filters.getMinPrice() != null) req = req.queryParam("minPrice", filters.getMinPrice());
        if (filters.getMaxPrice() != null) req = req.queryParam("maxPrice", filters.getMaxPrice());
        if (filters.getPage() != null)     req = req.queryParam("page", filters.getPage());
        if (filters.getSize() != null)     req = req.queryParam("size", filters.getSize());
        return req.get("/movies");
    }

    public Response getMovie(int mid) {
        return given().get("/movie/" + mid);
    }

    public Response createMovie(Movie movie) {
        return given().body(movie).post("/movie");
    }

    public Response updateMovie(int mid, Movie movie) {
        return given().body(movie).put("/movie/" + mid);
    }

    public Response deleteMovie(int mid) {
        return given().delete("/movie/" + mid);
    }

    public Response getMoviesByStudio(int sid) {
        return given().get("/studios/" + sid + "/movies");
    }
}