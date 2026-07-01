package com.apitest.contract;

import com.apitest.api.MoviesApi;
import com.apitest.base.BaseTest;
import com.apitest.model.Movie;
import com.apitest.model.MovieFilters;
import com.apitest.model.PageResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;

@Epic("movie-catalog-api")
@Feature("Movies Contract")
@Tag("contract")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MoviesContractTest extends BaseTest {

    private final MoviesApi moviesApi = new MoviesApi();
    private static final int CONTRACT_MID = 5099;

    @AfterAll
    void cleanup() {
        moviesApi.deleteMovie(CONTRACT_MID);
    }

    @Test
    void getMovies_responseMatchesPageMoviesSchema() {
        moviesApi.getMovies(MovieFilters.builder().size(100).build())
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/page-movies.json"));
    }

    @Test
    void getMovies_everyItemMatchesMovieSchema() {
        PageResponse<Movie> page = moviesApi.getMovies(MovieFilters.builder().size(100).build())
                .then().statusCode(200)
                .extract().as(new TypeRef<>() {});

        page.getContent().forEach(m -> {
            assertThat(m.getMid(), greaterThan(0));
            assertThat(m.getName(), not(emptyOrNullString()));
            assertThat(m.getGenre(), not(emptyOrNullString()));
            assertThat(m.getPrice(), greaterThanOrEqualTo(0.0));
            assertThat(m.getRating(), not(emptyOrNullString()));
            assertThat(m.getStudio(), greaterThan(0));
        });
    }

    @Test
    void getMovies_responseIncludesRequiredPaginationFields() {
        moviesApi.getMovies()
                .then()
                .statusCode(200)
                .body("totalElements", instanceOf(Integer.class))
                .body("page", instanceOf(Integer.class))
                .body("size", instanceOf(Integer.class))
                .body("totalPages", instanceOf(Integer.class));
    }

    @Test
    void getMovie_responseMatchesMovieSchema() {
        moviesApi.getMovie(1001)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/movie.json"));
    }

    @Test
    void getMovie_containsAllRequiredFieldsWithCorrectTypes() {
        moviesApi.getMovie(1001)
                .then()
                .statusCode(200)
                .body("mid", instanceOf(Integer.class))
                .body("name", instanceOf(String.class))
                .body("genre", instanceOf(String.class))
                .body("price", instanceOf(Number.class))
                .body("rating", instanceOf(String.class))
                .body("studio", instanceOf(Integer.class));
    }

    @Test
    void createMovie_createdResourceMatchesMovieSchema() {
        Movie payload = Movie.builder()
                .mid(CONTRACT_MID).name("Contract Test Movie").genre("Drama")
                .price(12.99).rating("PG").studio(1)
                .build();

        moviesApi.createMovie(payload)
                .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/movie.json"));
    }
}
