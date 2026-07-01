package com.apitest.smoke;

import com.apitest.api.MoviesApi;
import com.apitest.base.BaseTest;
import com.apitest.config.ConfigManager;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;

@Epic("movie-catalog-api")
@Feature("Movies")
@Tag("smoke")
class MoviesSmokeTest extends BaseTest {

    private final MoviesApi moviesApi = new MoviesApi();

    @Test
    void getMovies_returns200() {
        moviesApi.getMovies()
                .then()
                .statusCode(200)
                .time(lessThan((long) ConfigManager.getConfig().responseTimeThresholdMs()));
    }

    @Test
    void getMovie_bySeededId_returns200WithBody() {
        moviesApi.getMovie(1001)
                .then()
                .statusCode(200)
                .body("mid", equalTo(1001))
                .body("name", notNullValue())
                .time(lessThan((long) ConfigManager.getConfig().responseTimeThresholdMs()));
    }
}