import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CourierTest {
    private String courierId;
    private String login;
    private String password;
    private String firstName;
    private String registeredLogin;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/api/v1";
        login = "loginCourierAk47";
        password = "passCourierAk47";
        firstName = "Al Capone";
        registeredLogin = "ninja";
    }

    @Test
    @Step("Создание курьера")
    public void createCourier() {
        CourierDto json = new CourierDto(login, password, firstName);

        // Отправляем запрос на создание курьера
        Response response = given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("/courier")
                .then()
                .statusCode(201) // Проверяем, что курьер создан
                .body("ok", equalTo(true)) // Проверяем, что ответ успешный
                .extract()
                .response();

        // Авторизуемся и получаем ID курьера
        json.setLogin(login);
        json.setPassword(password);
        courierId = given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("/courier/login")
                .then()
                .statusCode(200) // Проверяем, что авторизация прошла успешно
                .body("id", notNullValue()) // Проверяем, что ID не пустой
                .extract()
                .jsonPath()
                .getString("id");
    }

    @Test
    @Step("Попытка создания дубликата курьера")
    public void duplicateCourierCreation() {
        CourierDto json = new CourierDto(login, password, firstName);

        // Создаем первого курьера
        given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("/courier")
                .then()
                .statusCode(201) // Проверяем, что курьер создан
                .body("ok", equalTo(true));

        // Пытаемся создать дубликат
        given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("/courier")
                .then()
                .statusCode(409) // Проверяем, что получаем ошибку
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой.")); // Проверяем текст ошибки

        // Авторизуемся для удаления курьера
        json.setLogin(login);
        json.setPassword(password);
        courierId = given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("/courier/login")
                .then()
                .body("id", notNullValue())
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("id");
    }

    @Test
    @Step("Создание курьера без обязательных полей")
    public void createCourierWithoutRequiredFields() {
        // Пытаемся создать курьера без пароля
        CourierDto jsonWithoutPassword = new CourierDto();
        jsonWithoutPassword.setLogin(login);
        jsonWithoutPassword.setFirstName(firstName);
        given()
                .contentType("application/json")
                .body(jsonWithoutPassword)
                .when()
                .post("/courier")
                .then()
                .statusCode(400) // Проверяем, что получаем ошибку
                .body("message", equalTo("Недостаточно данных для создания учетной записи")); // Проверяем текст ошибки

        // Пытаемся создать курьера без логина
        CourierDto jsonWithoutLogin = new CourierDto();
        jsonWithoutLogin.setPassword(password);
        jsonWithoutLogin.setFirstName(firstName);
        given()
                .contentType("application/json")
                .body(jsonWithoutLogin)
                .when()
                .post("/courier")
                .then()
                .statusCode(400) // Проверяем, что получаем ошибку
                .body("message", equalTo("Недостаточно данных для создания учетной записи")); // Проверяем текст ошибки
    }

    @Test
    @Step("Создание курьера с уже зарегистрированным логином")
    public void createCourierWithRegisteredLogin() {
        // Пытаемся создать курьера с уже существующим логином
        CourierDto json = new CourierDto();
        json.setLogin(registeredLogin);
        json.setPassword(password);
        json.setFirstName(firstName);
        Response response = given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("/courier")
                .then()
                .statusCode(409) // Проверяем, что получаем ошибку
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой.")) // Проверяем текст ошибки
                .extract()
                .response();
    }

    @After
    public void tearDown() {
        // Если у нас есть ID курьера, удаляем его
        if (courierId != null) {
            given()
                    .contentType("application/json")
                    .when()
                    .delete("/courier/" + courierId)
                    .then()
                    .statusCode(200); // Проверяем, что курьер успешно удалён
        }
    }
}
