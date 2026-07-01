package com.apitest.integration;

import com.apitest.api.MoviesApi;
import com.apitest.base.BaseTest;
import com.apitest.config.ConfigManager;
import com.apitest.model.Movie;
import com.apitest.model.MovieFilters;
import com.apitest.model.PageResponse;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;

@Epic("movie-catalog-api")
@Feature("Movies Integration")
@Tag("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MoviesApiTest extends BaseTest {

    private final MoviesApi moviesApi = new MoviesApi();

    private static final Movie TEST_MOVIE = Movie.builder()
            .mid(5001).name("Test Movie").genre("Action").price(9.99).rating("PG-13").studio(1)
            .build();

    @BeforeAll
    void ensureClean() {
        moviesApi.deleteMovie(TEST_MOVIE.getMid());
    }

    @AfterAll
    void cleanup() {
        moviesApi.deleteMovie(TEST_MOVIE.getMid());
    }

    @Test @Order(1)
    void getMovies_returns200WithPaginatedResponse() {
        moviesApi.getMovies()
                .then()
                .statusCode(200)
                .time(lessThan((long) ConfigManager.getConfig().responseTimeThresholdMs()))
                .body("content", instanceOf(java.util.List.class))
                .body("totalElements", greaterThan(0));
    }

    @Test @Order(2)
    void getMovies_contentMatchesMovieListSchema() {
        moviesApi.getMovies(MovieFilters.builder().size(100).build())
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/page-movies.json"));
    }

    @Test @Order(3)
    void getMovies_filtersByGenre() {
        PageResponse<Movie> page = moviesApi.getMovies(MovieFilters.builder().genre("Action").size(100).build())
                .then().statusCode(200)
                .extract().as(new TypeRef<>() {});

        assertThat(page.getContent().size(), greaterThan(0));
        page.getContent().forEach(m -> assertThat(m.getGenre(), equalTo("Action")));
    }

    @Test @Order(4)
    void getMovies_filtersByRating() {
        PageResponse<Movie> page = moviesApi.getMovies(MovieFilters.builder().rating("PG-13").size(100).build())
                .then().statusCode(200)
                .extract().as(new TypeRef<>() {});

        assertThat(page.getContent().size(), greaterThan(0));
        page.getContent().forEach(m -> assertThat(m.getRating(), equalTo("PG-13")));
    }

    @Test @Order(5)
    void getMovies_filtersByMaxPrice() {
        PageResponse<Movie> page = moviesApi.getMovies(MovieFilters.builder().maxPrice(5.0).size(100).build())
                .then().statusCode(200)
                .extract().as(new TypeRef<>() {});

        page.getContent().forEach(m -> assertThat(m.getPrice(), lessThanOrEqualTo(5.0)));
    }

    @Test @Order(6)
    void getMovies_filtersByMinPrice() {
        PageResponse<Movie> page = moviesApi.getMovies(MovieFilters.builder().minPrice(100.0).size(100).build())
                .then().statusCode(200)
                .extract().as(new TypeRef<>() {});

        assertThat(page.getContent().size(), greaterThan(0));
        page.getContent().forEach(m -> assertThat(m.getPrice(), greaterThanOrEqualTo(100.0)));
    }

    @Test @Order(7)
    void getMovies_paginatesCorrectly() {
        PageResponse<Movie> page0 = moviesApi.getMovies(MovieFilters.builder().page(0).size(5).build())
                .then().statusCode(200).extract().as(new TypeRef<>() {});
        PageResponse<Movie> page1 = moviesApi.getMovies(MovieFilters.builder().page(1).size(5).build())
                .then().statusCode(200).extract().as(new TypeRef<>() {});

        assertThat(page0.getContent().getFirst().getMid(), not(equalTo(page1.getContent().getFirst().getMid())));
    }

    @Test @Order(8)
    void getMovie_returnsSeededMovieById() {
        moviesApi.getMovie(1001)
                .then()
                .statusCode(200)
                .time(lessThan((long) ConfigManager.getConfig().responseTimeThresholdMs()))
                .body("mid", equalTo(1001))
                .body(matchesJsonSchemaInClasspath("schemas/movie.json"));
    }

    @Test @Order(9)
    void getMovie_returns404ForNonExistentMid() {
        moviesApi.getMovie(9999)
                .then()
                .statusCode(404);
    }

    @Test @Order(10)
    void createMovie_returns201() {
        moviesApi.createMovie(TEST_MOVIE)
                .then()
                .statusCode(201)
                .body("mid", equalTo(TEST_MOVIE.getMid()))
                .body("name", equalTo(TEST_MOVIE.getName()))
                .body(matchesJsonSchemaInClasspath("schemas/movie.json"));
    }

    @Test @Order(11)
    void createMovie_returns409WhenMidExists() {
        moviesApi.createMovie(TEST_MOVIE)
                .then()
                .statusCode(409);
    }

    @Test @Order(12)
    void createMovie_returns201WhenStudioIdNotInDb() {
        Movie orphan = Movie.builder().mid(5010).name("Orphan Movie").genre("Action")
                .price(5.0).rating("PG").studio(77).build();
        moviesApi.deleteMovie(orphan.getMid());

        moviesApi.createMovie(orphan)
                .then()
                .statusCode(201)
                .body("studio", equalTo(77));

        moviesApi.deleteMovie(orphan.getMid());
    }

    @Test @Order(13)
    void createMovie_returns400WhenMidBelowMinimum() {
        moviesApi.createMovie(Movie.builder().mid(999).name("Bad").genre("Action")
                        .price(5.0).rating("PG").studio(1).build())
                .then()
                .statusCode(400)
                .body("message", equalTo("Spring Validation Error"))
                .body("errors.field", hasItem("mid"));
    }

    @Test @Order(14)
    void createMovie_returns400WhenGenreIsInvalid() {
        moviesApi.createMovie(Movie.builder().mid(5002).name("Bad").genre("Cartoon")
                        .price(5.0).rating("PG").studio(1).build())
                .then()
                .statusCode(400)
                .body("errors.field", hasItem("genre"));
    }

    @Test @Order(15)
    void createMovie_returns400WhenRatingIsInvalid() {
        moviesApi.createMovie(Movie.builder().mid(5003).name("Bad").genre("Action")
                        .price(5.0).rating("X").studio(1).build())
                .then()
                .statusCode(400)
                .body("errors.field", hasItem("rating"));
    }

    @Test @Order(16)
    void updateMovie_updatesExistingMovie() {
        Movie updated = Movie.builder().mid(TEST_MOVIE.getMid()).name("Updated Movie")
                .genre(TEST_MOVIE.getGenre()).price(TEST_MOVIE.getPrice())
                .rating(TEST_MOVIE.getRating()).studio(TEST_MOVIE.getStudio()).build();

        moviesApi.updateMovie(TEST_MOVIE.getMid(), updated)
                .then()
                .statusCode(200)
                .body("name", equalTo("Updated Movie"));
    }

    @Test @Order(17)
    void updateMovie_returns404ForNonExistentMid() {
        moviesApi.updateMovie(9999, Movie.builder().mid(9999).name("Ghost").genre("Action")
                        .price(5.0).rating("PG").studio(1).build())
                .then()
                .statusCode(404);
    }

    @Test @Order(18)
    void getMoviesByStudio_returnsMoviesForKnownStudio() {
        moviesApi.getMoviesByStudio(1)
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("studio", everyItem(equalTo(1)));
    }

    @Test @Order(19)
    void getMoviesByStudio_returns404ForStudioWithNoMovies() {
        moviesApi.getMoviesByStudio(99)
                .then()
                .statusCode(404);
    }

    @Test @Order(20)
    void deleteMovie_returns200WithDeletedMovie() {
        moviesApi.deleteMovie(TEST_MOVIE.getMid())
                .then()
                .statusCode(200)
                .body("mid", equalTo(TEST_MOVIE.getMid()));
    }

    @Test @Order(21)
    void deleteMovie_returns404AfterDeletion() {
        moviesApi.deleteMovie(TEST_MOVIE.getMid())
                .then()
                .statusCode(404);
    }
}
