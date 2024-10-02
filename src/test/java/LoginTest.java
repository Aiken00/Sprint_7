import io.qameta.allure.Step;
import io.restassured.RestAssured;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class LoginTest {
    private String courierId;
    private String login;
    private String password;
    private String firstName;
    private String invalidLogin;
    private String invalidPassword;

    @Before
    public void setUp() {
        // Устанавливаем базовый URI для API
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/api/v1";
        login = "shelby2281337"; // Логин для курьера
        password = "12345"; // Пароль для курьера
        firstName = "Thomas"; // Имя курьера
        invalidLogin = "SAWDIJAWODKO213123_23"; // Неверный логин
        invalidPassword = "awdi12j3i1j23adawddd"; // Неверный пароль
    }

    @Test
    @Step("Авторизация курьера с валидными данными")
    public void loginCourier() {
        // Создаем объект курьера с валидными данными
        CourierDto jsonCreateCourier = new CourierDto(login, password, firstName);

        // Отправляем запрос на создание курьера
        given()
                .contentType("application/json")
                .body(jsonCreateCourier)
                .when()
                .post("/courier")
                .then()
                .statusCode(201) // Проверяем, что курьер создан
                .body("ok", equalTo(true)); // Проверяем, что ответ успешный

        // Создаем объект для авторизации
        LoginDto json = new LoginDto(login, password);

        // Отправляем запрос на авторизацию курьера
        courierId = given()
                .contentType("application/json")
                .body(json)
                .when()
                .post("/courier/login")
                .then()
                .statusCode(200) // Проверяем, что авторизация прошла успешно
                .body("id", notNullValue()) // Проверяем, что ID курьера не пустой
                .extract().jsonPath().getString("id"); // Извлекаем ID курьера
    }

    @Test
    @Step("Авторизация курьера с невалидными данными")
    public void loginWithInvalidCredentials() {
        // Создаем объект с невалидными данными для авторизации
        LoginDto jsonWithInvalidCredentials = new LoginDto(invalidLogin, invalidPassword);

        // Отправляем запрос на авторизацию
        given()
                .contentType("application/json")
                .body(jsonWithInvalidCredentials)
                .when()
                .post("/courier/login")
                .then()
                .statusCode(404) // Проверяем, что получаем ошибку
                .body("message", equalTo("Учетная запись не найдена")); // Проверяем текст ошибки
    }

    @Test
    @Step("Авторизация курьера с недостающими полями")
    public void loginWithoutRequiredField() {
        // Пытаемся авторизоваться без пароля
        LoginDto jsonWithoutPassword = new LoginDto();
        jsonWithoutPassword.setLogin(login); // Устанавливаем логин
        given()
                .contentType("application/json")
                .body(jsonWithoutPassword)
                .when()
                .post("/courier/login")
                .then()
                .statusCode(504); // Проверяем, что получаем ошибку

        // Пытаемся авторизоваться без логина
        LoginDto jsonWithoutLogin = new LoginDto();
        jsonWithoutLogin.setPassword(password); // Устанавливаем пароль
        given()
                .contentType("application/json")
                .body(jsonWithoutLogin)
                .when()
                .post("/courier/login")
                .then()
                .statusCode(400); // Проверяем, что получаем ошибку

        // Пытаемся авторизоваться с пустыми полями
        LoginDto jsonEmptyFields = new LoginDto("", "");
        given()
                .contentType("application/json")
                .body(jsonEmptyFields)
                .when()
                .post("/courier/login")
                .then()
                .statusCode(400) // Проверяем, что получаем ошибку
                .body("message", equalTo("Недостаточно данных для входа")); // Проверяем текст ошибки
    }

    @After
    @Step("Удаление учётной записи курьера")
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
