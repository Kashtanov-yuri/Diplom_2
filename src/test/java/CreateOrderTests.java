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

public class CreateOrderTests {

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
    @DisplayName("Создание заказа с авторизацией и ингредиентами")
    @Description("Проверка успешного создания заказа авторизованным пользователем с валидными ингредиентами")
    public void createOrderWithAuthorizationAndIngredientsShouldReturnSuccess() {
        createTestUser();
        List<String> ingredients = Arrays.asList(
                "61c0c5a71d1f82001bdaaa6d", // Флюоресцентная булка R2-D3
                "61c0c5a71d1f82001bdaaa6f"  // Биокотлета из марсианской Магнолии
        );

        createOrderWithAuthorization(ingredients)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue())
                .body("order.ingredients", not(empty()));
    }

    @Test
    @DisplayName("Создание заказа без авторизации с ингредиентами")
    @Description("Проверка успешного создания заказа неавторизованным пользователем с валидными ингредиентами")
    public void createOrderWithoutAuthorizationWithIngredientsShouldReturnSuccess() {
        List<String> ingredients = Arrays.asList(
                "61c0c5a71d1f82001bdaaa6d", // Флюоресцентная булка R2-D3
                "61c0c5a71d1f82001bdaaa6f"  // Биокотлета из марсианской Магнолии
        );

        createOrderWithoutAuthorization(ingredients)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue())
                .body("order.ingredients", not(empty()));
    }

    @Test
    @DisplayName("Создание заказа с авторизацией без ингредиентов")
    @Description("Проверка ошибки при создании заказа без ингредиентов")
    public void createOrderWithAuthorizationWithoutIngredientsShouldReturnError() {
        createTestUser();
        List<String> ingredients = Arrays.asList();

        createOrderWithAuthorization(ingredients)
                .then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа без авторизации без ингредиентов")
    @Description("Проверка ошибки при создании заказа без ингредиентов и без авторизации")
    public void createOrderWithoutAuthorizationWithoutIngredientsShouldReturnError() {
        List<String> ingredients = Arrays.asList();

        createOrderWithoutAuthorization(ingredients)
                .then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиентов")
    @Description("Проверка ошибки при создании заказа с невалидными хешами ингредиентов")
    public void createOrderWithInvalidIngredientHashShouldReturnError() {
        createTestUser();
        List<String> ingredients = Arrays.asList(
                "invalid_hash_1",
                "invalid_hash_2"
        );

        createOrderWithAuthorization(ingredients)
                .then()
                .statusCode(500);
    }

    @Test
    @DisplayName("Создание заказа с одним ингредиентом")
    @Description("Проверка создания заказа с минимальным количеством ингредиентов")
    public void createOrderWithSingleIngredientShouldReturnSuccess() {
        createTestUser();
        List<String> ingredients = Arrays.asList("61c0c5a71d1f82001bdaaa6d");

        createOrderWithAuthorization(ingredients)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа с несколькими ингредиентами")
    @Description("Проверка создания заказа с большим количеством ингредиентов")
    public void createOrderWithMultipleIngredientsShouldReturnSuccess() {
        createTestUser();
        List<String> ingredients = Arrays.asList(
                "61c0c5a71d1f82001bdaaa6d", // Флюоресцентная булка R2-D3
                "61c0c5a71d1f82001bdaaa6f", // Биокотлета из марсианской Магнолии
                "61c0c5a71d1f82001bdaaa72", // Соус Spicy-X
                "61c0c5a71d1f82001bdaaa76"  // Сыр с астероидной плесенью
        );

        createOrderWithAuthorization(ingredients)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order.number", notNullValue())
                .body("order.ingredients", hasSize(4));
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

    @Step("Создание заказа с авторизацией")
    private Response createOrderWithAuthorization(List<String> ingredients) {
        return OrderClient.createOrder(accessToken, ingredients);
    }

    @Step("Создание заказа без авторизации")
    private Response createOrderWithoutAuthorization(List<String> ingredients) {
        return OrderClient.createOrder(null, ingredients);
    }
}
