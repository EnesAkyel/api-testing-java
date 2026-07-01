package com.apitest.regression;

import com.apitest.api.MoviesApi;
import com.apitest.api.StudiosApi;
import com.apitest.base.BaseTest;
import com.apitest.model.Movie;
import com.apitest.model.MovieFilters;
import com.apitest.model.PageResponse;
import com.apitest.model.Studio;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Epic("movie-catalog-api")
@Feature("Movies Regression")
@Tag("regression")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MoviesRegressionTest extends BaseTest {

    private final MoviesApi moviesApi = new MoviesApi();
    private final StudiosApi studiosApi = new StudiosApi();

    // Test data — MIDs above 5000 avoid collisions with seeded fixture data
    private static final Movie REGRESSION_MOVIE = Movie.builder()
            .mid(5050).name("Regression Movie").genre("Comedy").price(7.99).rating("G").studio(1)
            .build();

    @BeforeAll
    void ensureClean() {
        moviesApi.deleteMovie(REGRESSION_MOVIE.getMid());
        moviesApi.deleteMovie(5051);
        moviesApi.deleteMovie(5052);
        moviesApi.deleteMovie(5053);
    }

    @AfterAll
    void cleanup() {
        moviesApi.deleteMovie(REGRESSION_MOVIE.getMid());
        moviesApi.deleteMovie(5051);
        moviesApi.deleteMovie(5052);
        moviesApi.deleteMovie(5053);
    }

    // GET /movies — collection integrity

    @Test @Order(1)
    void getMovies_returnsAllSeededMovies() {
        PageResponse<Movie> page = moviesApi.getMovies(MovieFilters.builder().size(100).build())
                .then().statusCode(200)
                .extract().as(new TypeRef<PageResponse<Movie>>() {});

        assertThat(page.getTotalElements(), greaterThanOrEqualTo(30));
    }

    @Test @Order(2)
    void getMovies_everyMovieHasUniqueMid() {
        List<Integer> mids = moviesApi.getMovies(MovieFilters.builder().size(100).build())
                .then().statusCode(200)
                .extract().as(new TypeRef<PageResponse<Movie>>() {})
                .getContent().stream().map(Movie::getMid).collect(Collectors.toList());

        assertThat(Set.copyOf(mids).size(), equalTo(mids.size()));
    }

    @Test @Order(3)
    void getMovies_allMoviesMatchSchema() {
        moviesApi.getMovies(MovieFilters.builder().size(100).build())
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/page-movies.json"));
    }

    @Test @Order(4)
    void getMovies_allStudioIdsReferenceValidSeededStudio() {
        PageResponse<Movie> movies = moviesApi.getMovies(MovieFilters.builder().size(100).build())
                .then().statusCode(200).extract().as(new TypeRef<PageResponse<Movie>>() {});
        PageResponse<Studio> studios = studiosApi.getStudios(0, 100)
                .then().statusCode(200).extract().as(new TypeRef<PageResponse<Studio>>() {});

        Set<Integer> validSids = studios.getContent().stream()
                .map(Studio::getSid).collect(Collectors.toSet());

        List<Movie> orphaned = movies.getContent().stream()
                .filter(m -> !validSids.contains(m.getStudio()))
                .collect(Collectors.toList());

        assertThat(orphaned, hasSize(0));
    }

    // GET /movie/:mid — individual retrieval

    @ParameterizedTest @Order(5)
    @ValueSource(ints = {1001, 1010, 1030})
    void getMovie_isRetrievableAndMatchesSchema(int mid) {
        moviesApi.getMovie(mid)
                .then()
                .statusCode(200)
                .body("mid", equalTo(mid))
                .body(matchesJsonSchemaInClasspath("schemas/movie.json"));
    }

    @Test @Order(6)
    void getMovie_individualDataMatchesCorrespondingEntryInFullList() {
        Movie single = moviesApi.getMovie(1001)
                .then().statusCode(200).extract().as(Movie.class);

        Movie fromList = moviesApi.getMovies(MovieFilters.builder().size(100).build())
                .then().statusCode(200).extract().as(new TypeRef<PageResponse<Movie>>() {})
                .getContent().stream()
                .filter(m -> m.getMid() == 1001)
                .findFirst().orElseThrow();

        assertThat(single, equalTo(fromList));
    }

    // POST /movie — write operations

    @Test @Order(10)
    void createMovie_returns201WithCorrectMid() {
        moviesApi.createMovie(REGRESSION_MOVIE)
                .then()
                .statusCode(201)
                .body("mid", equalTo(REGRESSION_MOVIE.getMid()));
    }

    @Test @Order(11)
    void createMovie_echoesSubmittedPayload() {
        Movie echo = Movie.builder().mid(5051).name(REGRESSION_MOVIE.getName())
                .genre(REGRESSION_MOVIE.getGenre()).price(REGRESSION_MOVIE.getPrice())
                .rating(REGRESSION_MOVIE.getRating()).studio(REGRESSION_MOVIE.getStudio()).build();

        moviesApi.createMovie(echo)
                .then()
                .statusCode(201)
                .body("name", equalTo(REGRESSION_MOVIE.getName()))
                .body("genre", equalTo(REGRESSION_MOVIE.getGenre()))
                .body("price", equalTo((float) REGRESSION_MOVIE.getPrice()));
    }

    // PUT /movie/:mid — update

    @Test @Order(12)
    void setup_createMovieForUpdateTest() {
        Movie payload = Movie.builder().mid(5052).name("Before Update").genre("Horror")
                .price(14.99).rating("R").studio(2).build();
        moviesApi.deleteMovie(payload.getMid());
        moviesApi.createMovie(payload).then().statusCode(201);
    }

    @Test @Order(13)
    void updateMovie_reflectsAllSubmittedFields() {
        Movie updated = Movie.builder().mid(5052).name("After Update").genre("Thriller")
                .price(14.99).rating("R").studio(2).build();

        moviesApi.updateMovie(5052, updated)
                .then()
                .statusCode(200)
                .body("name", equalTo("After Update"))
                .body("genre", equalTo("Thriller"));
    }

    // DELETE /movie/:mid

    @Test @Order(14)
    void setup_createMovieForDeleteTest() {
        Movie payload = Movie.builder().mid(5053).name("To Delete").genre("Mystery")
                .price(5.99).rating("PG").studio(3).build();
        moviesApi.deleteMovie(payload.getMid());
        moviesApi.createMovie(payload).then().statusCode(201);
    }

    @Test @Order(15)
    void deleteMovie_returns200OnSuccessfulDeletion() {
        moviesApi.deleteMovie(5053)
                .then()
                .statusCode(200);
    }
}