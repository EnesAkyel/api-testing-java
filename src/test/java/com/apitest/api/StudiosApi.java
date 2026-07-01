package com.apitest.api;

import com.apitest.model.Studio;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class StudiosApi {

    public Response getStudios() {
        return given().queryParam("page", 0).queryParam("size", 10).get("/studios");
    }

    public Response getStudios(int page, int size) {
        return given().queryParam("page", page).queryParam("size", size).get("/studios");
    }

    public Response createStudio(Studio studio) {
        return given().body(studio).post("/studio");
    }

    public Response updateStudio(int sid, Studio studio) {
        return given().body(studio).put("/studio/" + sid);
    }

    public Response deleteStudio(int sid) {
        return given().delete("/studio/" + sid);
    }
}