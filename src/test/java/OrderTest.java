import io.qameta.allure.Step;
import io.restassured.RestAssured;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(Parameterized.class)
public class OrderTest {
    private String firstName; // Имя заказчика
    private String lastName; // Фамилия заказчика
    private String address; // Адрес доставки
    private int metroStation; // ID ближайшей станции метро
    private String phone; // Номер телефона
    private int rentTime; // Время аренды
    private String deliveryDate; // Дата доставки
    private String comment; // Комментарий к заказу
    private ArrayList<String> color; // Цвет самоката
    private int trackId; // ID отслеживания заказа

    @Parameterized.Parameter(0)
    public String colorDescription; // Описание параметра цвета

    @Parameterized.Parameter(1)
    public ArrayList<String> colorParam; // Параметры цвета

    @Before
    public void setUp() {
        // Устанавливаем базовый URI для API
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/api/v1";

        // Устанавливаем значения для теста
        firstName = "Naruto";
        lastName = "Uchiha";
        address = "Konoha, 142 apt";
        metroStation = 4;
        phone = "+7 912 305 91 91";
        rentTime = 5;
        deliveryDate = "2020-06-06";
        comment = "Naruto, come back to Konoha";
        color = colorParam; // Устанавливаем цвет на основе параметров
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        // Параметризованные данные для тестов
        return Arrays.asList(new Object[][]{
                {"Один цвет - Black", new ArrayList<>(Arrays.asList("Black"))},
                {"Один цвет - Grey", new ArrayList<>(Arrays.asList("Grey"))},
                {"Оба цвета", new ArrayList<>(Arrays.asList("Black", "Grey"))},
                {"Без цвета", new ArrayList<>()}
        });
    }

    @Test
    @Step("Создание заказа с цветами: {0}")
    public void orderCreationTest() {
        // Создаем объект заказа с заданными параметрами
        OrderDto orderDto = new OrderDto(firstName, lastName, address, metroStation, phone, rentTime, deliveryDate, comment, color);

        // Извлечение track ID из ответа
        trackId = given()
                .contentType("application/json")
                .body(orderDto) // Отправляем тело запроса
                .when()
                .post("/orders") // Запрос на создание заказа
                .then()
                .statusCode(201) // Проверяем, что статус ответа 201 (Создано)
                .body("track", notNullValue()) // Проверяем, что track ID не null
                .extract()
                .path("track"); // Извлекаем track ID из ответа
    }

    @After
    public void tearDown() {
        // Если trackId установлен, отменяем заказ
        if (trackId != 0) {
            RestAssured.given()
                    .contentType("application/json")
                    .queryParam("track", trackId) // Указываем track ID
                    .when()
                    .put("/orders/cancel") // Запрос на отмену заказа
                    .then()
                    .statusCode(200) // Проверяем, что статус ответа 200 (ОК)
                    .body("ok", equalTo(true)); // Проверяем, что отмена прошла успешно
        }
    }
}
