package com.apitest.contract;

import com.apitest.api.StudiosApi;
import com.apitest.base.BaseTest;
import com.apitest.model.PageResponse;
import com.apitest.model.Studio;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.common.mapper.TypeRef;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;

@Epic("movie-catalog-api")
@Feature("Studios Contract")
@Tag("contract")
class StudiosContractTest extends BaseTest {

    private final StudiosApi studiosApi = new StudiosApi();

    @Test
    void getStudios_responseMatchesPageStudiosSchema() {
        studiosApi.getStudios(0, 100)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/page-studios.json"));
    }

    @Test
    void getStudios_everyItemMatchesStudioSchema() {
        PageResponse<Studio> page = studiosApi.getStudios(0, 100)
                .then().statusCode(200)
                .extract().as(new TypeRef<>() {});

        page.getContent().forEach(s -> {
            assertThat(s.getSid(), greaterThan(0));
            assertThat(s.getName(), not(emptyOrNullString()));
        });
    }

    @Test
    void getStudios_responseIncludesRequiredPaginationFields() {
        studiosApi.getStudios()
                .then()
                .statusCode(200)
                .body("totalElements", instanceOf(Integer.class))
                .body("page", instanceOf(Integer.class))
                .body("size", instanceOf(Integer.class))
                .body("totalPages", instanceOf(Integer.class));
    }
}
