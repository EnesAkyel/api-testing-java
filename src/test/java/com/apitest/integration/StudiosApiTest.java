package com.apitest.integration;

import com.apitest.api.StudiosApi;
import com.apitest.base.BaseTest;
import com.apitest.config.ConfigManager;
import com.apitest.model.PageResponse;
import com.apitest.model.Studio;
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
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.lessThan;

@Epic("movie-catalog-api")
@Feature("Studios Integration")
@Tag("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StudiosApiTest extends BaseTest {

    private final StudiosApi studiosApi = new StudiosApi();

    private static final Studio TEST_STUDIO = Studio.builder()
            .sid(50).name("Test Studio")
            .build();

    @BeforeAll
    void ensureClean() {
        studiosApi.deleteStudio(TEST_STUDIO.getSid());
    }

    @AfterAll
    void cleanup() {
        studiosApi.deleteStudio(TEST_STUDIO.getSid());
    }

    @Test @Order(1)
    void getStudios_returns200WithPaginatedResponse() {
        studiosApi.getStudios()
                .then()
                .statusCode(200)
                .time(lessThan((long) ConfigManager.getConfig().responseTimeThresholdMs()))
                .body("content", instanceOf(java.util.List.class))
                .body("totalElements", greaterThan(0));
    }

    @Test @Order(2)
    void getStudios_contentMatchesStudioListSchema() {
        studiosApi.getStudios(0, 100)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/page-studios.json"));
    }

    @Test @Order(3)
    void getStudios_returnsAtLeast5SeededStudios() {
        PageResponse<Studio> page = studiosApi.getStudios(0, 100)
                .then().statusCode(200)
                .extract().as(new TypeRef<>() {});

        assertThat(page.getTotalElements(), greaterThanOrEqualTo(5));
    }

    @Test @Order(4)
    void createStudio_returns201() {
        studiosApi.createStudio(TEST_STUDIO)
                .then()
                .statusCode(201)
                .body("sid", equalTo(TEST_STUDIO.getSid()))
                .body("name", equalTo(TEST_STUDIO.getName()))
                .body(matchesJsonSchemaInClasspath("schemas/studio.json"));
    }

    @Test @Order(5)
    void createStudio_returns409WhenSidExists() {
        studiosApi.createStudio(TEST_STUDIO)
                .then()
                .statusCode(409);
    }

    @Test @Order(6)
    void updateStudio_updatesExistingStudio() {
        Studio updated = Studio.builder().sid(TEST_STUDIO.getSid()).name("Updated Studio").build();

        studiosApi.updateStudio(TEST_STUDIO.getSid(), updated)
                .then()
                .statusCode(200)
                .body("name", equalTo("Updated Studio"));
    }

    @Test @Order(7)
    void updateStudio_returns404ForNonExistentSid() {
        studiosApi.updateStudio(99, Studio.builder().sid(99).name("Ghost").build())
                .then()
                .statusCode(404);
    }

    @Test @Order(8)
    void deleteStudio_returns200WithDeletedStudio() {
        studiosApi.deleteStudio(TEST_STUDIO.getSid())
                .then()
                .statusCode(200)
                .body("sid", equalTo(TEST_STUDIO.getSid()));
    }

    @Test @Order(9)
    void deleteStudio_returns404AfterDeletion() {
        studiosApi.deleteStudio(TEST_STUDIO.getSid())
                .then()
                .statusCode(404);
    }
}
