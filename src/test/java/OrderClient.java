import io.qameta.allure.Step;
import io.restassured.response.Response;
import java.util.List;
import static io.restassured.RestAssured.given;

public class OrderClient {

    private static final String ORDER_URL = "/api/orders";
    private static final String USER_ORDERS_URL = "/api/orders";

    @Step("Создание заказа")
    public static Response createOrder(String accessToken, List<String> ingredients) {
        StringBuilder ingredientsJson = new StringBuilder();
        ingredientsJson.append("{\"ingredients\":[");

        for (int i = 0; i < ingredients.size(); i++) {
            if (i > 0) {
                ingredientsJson.append(",");
            }
            ingredientsJson.append("\"").append(ingredients.get(i)).append("\"");
        }
        ingredientsJson.append("]}");

        if (accessToken != null && !accessToken.isEmpty()) {
            return given()
                    .header("Content-type", "application/json")
                    .header("Authorization", accessToken)
                    .body(ingredientsJson.toString())
                    .post(ORDER_URL);
        } else {
            return given()
                    .header("Content-type", "application/json")
                    .body(ingredientsJson.toString())
                    .post(ORDER_URL);
        }
    }

    @Step("Получение заказов пользователя")
    public static Response getUserOrders(String accessToken) {
        if (accessToken != null && !accessToken.isEmpty()) {
            return given()
                    .header("Authorization", accessToken)
                    .get(USER_ORDERS_URL);
        } else {
            return given()
                    .get(USER_ORDERS_URL);
        }
    }
}