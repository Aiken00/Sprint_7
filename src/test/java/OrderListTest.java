import io.qameta.allure.Step;
import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class OrderListTest {
    private int courierId; // ID курьера
    private int nearestStation; // ID ближайшей станции
    private int limit; // Ограничение на количество заказов
    private int page; // Номер страницы

    @Before
    public void setUp() {
        // Устанавливаем базовый URI для API
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/api/v1";

        // Устанавливаем значения для теста
        courierId = 387417; // Пример ID курьера
        nearestStation = 3; // Пример ID ближайшей станции
        limit = 30; // Ограничение на 30 заказов
        page = 0; // Первая страница
    }

    @Test
    @Step("Получение списка заказов")
    public void getOrderListTest() {
        // Отправляем запрос для получения списка заказов
        given()
                .queryParams("courierId", courierId,
                        "nearestStation", nearestStation,
                        "limit", limit,
                        "page", page)
                .when()
                .get("/orders") // Запрос на получение списка заказов
                .then()
                .statusCode(200) // Проверяем, что статус ответа 200 (ОК)
                .body("orders", notNullValue()) // Проверяем, что список заказов не null
                .body("orders.size()", greaterThan(0)); // Проверяем, что размер списка заказов больше 0
    }
}
