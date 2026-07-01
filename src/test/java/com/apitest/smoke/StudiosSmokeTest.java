package com.apitest.smoke;

import com.apitest.api.StudiosApi;
import com.apitest.base.BaseTest;
import com.apitest.config.ConfigManager;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.lessThan;

@Epic("movie-catalog-api")
@Feature("Studios")
@Tag("smoke")
class StudiosSmokeTest extends BaseTest {

    private final StudiosApi studiosApi = new StudiosApi();

    @Test
    void getStudios_returns200() {
        studiosApi.getStudios()
                .then()
                .statusCode(200)
                .time(lessThan((long) ConfigManager.getConfig().responseTimeThresholdMs()));
    }
}