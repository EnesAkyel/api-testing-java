package com.apitest.auth;

import com.apitest.config.ConfigManager;
import io.restassured.http.ContentType;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class AuthUtil {
    private static String cachedToken;

    private AuthUtil() {}

    public static synchronized String getToken() {
        if (cachedToken == null) {
            cachedToken = given()
                    .baseUri(ConfigManager.getConfig().baseUrl())
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "username", System.getenv().getOrDefault("AUTH_USERNAME", ""),
                            "password", System.getenv().getOrDefault("AUTH_PASSWORD", "")
                    ))
                    .post("/auth/login")
                    .then().statusCode(200)
                    .extract().jsonPath().getString("token");
        }
        return cachedToken;
    }
}
