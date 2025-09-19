import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.Matchers.*;

public class GetUserOrdersTests {

    private String userEmail;
    private String userPassword;
    private String userName;
    private String accessToken;

    @BeforeEach
    @Step("Настройка базового URL")
    public void setup() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
    }

    @AfterEach
    @Step("Удаление тестового пользователя")
    public void tearDown() {
        if (accessToken != null) {
            UserClient.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Получение заказов авторизованного пользователя с заказами")
    @Description("Проверка получения заказов авторизованного пользователя, у которого есть заказы")
    public void getOrdersForAuthorizedUserWithOrdersShouldReturnSuccess() {
        createTestUser();
        createTestOrder();

        getUserOrdersWithAuthorization()
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("orders", not(empty()))
                .body("orders[0].number", notNullValue())
                .body("orders[0].ingredients", not(empty()));
    }

    @Test
    @DisplayName("Получение заказов авторизованного пользователя без заказов")
    @Description("Проверка получения заказов авторизованного пользователя, у которого нет заказов")
    public void getOrdersForAuthorizedUserWithoutOrdersShouldReturnEmptyList() {
        createTestUser();

        getUserOrdersWithAuthorization()
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("orders", empty());
    }

    @Test
    @DisplayName("Получение заказов неавторизованного пользователя")
    @Description("Проверка ошибки при получении заказов неавторизованного пользователя")
    public void getOrdersForUnauthorizedUserShouldReturnError() {
        getUserOrdersWithoutAuthorization()
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Test
    @DisplayName("Получение заказов с невалидным токеном")
    @Description("Проверка ошибки при получении заказов с невалидным токеном авторизации")
    public void getOrdersWithInvalidTokenShouldReturnError() {
        getUserOrdersWithInvalidToken()
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Test
    @DisplayName("Получение заказов после создания нескольких заказов")
    @Description("Проверка получения всех заказов пользователя после создания нескольких заказов")
    public void getOrdersAfterMultipleOrdersShouldReturnAllOrders() {
        createTestUser();
        createTestOrder();
        createTestOrder();
        createTestOrder();

        getUserOrdersWithAuthorization()
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("orders", hasSize(greaterThanOrEqualTo(3)))
                .body("orders[0].number", notNullValue())
                .body("orders[1].number", notNullValue())
                .body("orders[2].number", notNullValue());
    }

    @Step("Создание тестового пользователя")
    private void createTestUser() {
        userEmail = UserClient.generateUniqueEmail();
        userPassword = "password";
        userName = "Test User";

        UserClient.createUser(userEmail, userPassword, userName);
        Response loginResponse = UserClient.login(userEmail, userPassword);
        accessToken = loginResponse.then()
                .extract()
                .path("accessToken");
    }

    @Step("Создание тестового заказа")
    private void createTestOrder() {
        List<String> ingredients = Arrays.asList(
                "61c0c5a71d1f82001bdaaa6d", // Флюоресцентная булка R2-D3
                "61c0c5a71d1f82001bdaaa6f"  // Биокотлета из марсианской Магнолии
        );

        OrderClient.createOrder(accessToken, ingredients)
                .then()
                .statusCode(200);
    }

    @Step("Получение заказов пользователя с авторизацией")
    private Response getUserOrdersWithAuthorization() {
        return OrderClient.getUserOrders(accessToken);
    }

    @Step("Получение заказов пользователя без авторизации")
    private Response getUserOrdersWithoutAuthorization() {
        return OrderClient.getUserOrders(null);
    }

    @Step("Получение заказов пользователя с невалидным токеном")
    private Response getUserOrdersWithInvalidToken() {
        return OrderClient.getUserOrders("invalid_token_123");
    }
}
