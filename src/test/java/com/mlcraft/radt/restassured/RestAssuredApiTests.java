package com.mlcraft.radt.restassured;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@Slf4j
public class RestAssuredApiTests {

    public static final String BASEURL = "https://api.restful-api.dev/objects";

    @Test
    public void testGetAll() {
        given().get(BASEURL).then()
                .statusCode(200)
                .assertThat().body("size()", is(13),
                        "id[0]", equalTo("1"),
                        "name[0]", equalTo("Google Pixel 6 Pro"),
                        "data[0].color", equalTo("Cloudy White"),
                        "data[0].capacity", equalTo("128 GB"));

    }

    @Test
    public void testGetMultipleObjectByIdsAsRequestParams() {
        given().queryParam("id", 3).
                queryParam("id", 7)
                .queryParam("id", 9)
                .get(BASEURL).then()
                .statusCode(200)
                .assertThat().body("id[0]", equalTo("3"),
                        "id[1]", equalTo("7"),
                        "id[2]", equalTo("9"));
    }

    @Test
    public void testGetObjectWithInvalidId() {
        given().queryParam("id", 234)
                .get(BASEURL).then()
                .statusCode(200)
                .assertThat().body("size()", equalTo(0));
    }

    @Test
    public void testListOfObjectsByIdsAndWithOneInvalidId() {
        given().queryParam("id", 234).
                queryParam("id", 7)
                .queryParam("id", 9)
                .get(BASEURL).then()
                .statusCode(200)
                .assertThat().body("id[0]", equalTo("7"),
                        "id[1]", equalTo("9"),
                        "size()", equalTo(2));
    }

    @Test
    public void testGetOneObjectUsingPathParam() {
        given().pathParam("id", "5")
                .get(String.format("%s/{id}", BASEURL)).then()
                .statusCode(200)
                .assertThat().body("id", equalTo("5"),
                        "name", equalTo("Samsung Galaxy Z Fold2"),
                        "data.price", equalTo(689.99f),
                        "data.color", equalTo("Brown"));
    }

    @Test
    public void testGetNotFoundObjectUsingPathParamsByInvalidId() {
        given().pathParam("id", "23434")
                .get(String.format("%s/{id}", BASEURL)).then()
                .statusCode(404)
                .assertThat().body("error", equalTo("Oject with id=23434 was not found."));
    }

    @Test
    public void testCreateNewObject() {
        try {
            JSONObject testObject = new JSONObject().put("name","Apple MacBook Pro 16")
                    .put("data", new JSONObject().put("year", 2019)
                            .put("price", 1849.99f)
                            .put("CPU model", "Intel Core i9")
                            .put("Hard disk size", "1 TB"));
            given().contentType("application/json").body(testObject.toString()).when().post(BASEURL).then()
                    .statusCode(200)
                    .assertThat().body("name", equalTo("Apple MacBook Pro 16"),
                            "data.price", equalTo(1849.99f),
                            "data.\"CPU model\"", equalTo("Intel Core i9"),
                            "data.\"Hard disk size\"", equalTo("1 TB"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
