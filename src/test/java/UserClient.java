import io.qameta.allure.Step;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

public class UserClient {

    private static final String REGISTER_URL = "/api/auth/register";
    private static final String LOGIN_URL = "/api/auth/login";
    private static final String USER_URL = "/api/auth/user";

    @Step("Создание пользователя: email = {email}, name = {name}")
    public static Response createUser(String email, String password, String name) {
        return given()
                .header("Content-type", "application/json")
                .body(String.format("{\"email\": \"%s\", \"password\": \"%s\", \"name\": \"%s\"}",
                        email, password, name))
                .post(REGISTER_URL);
    }


    @Step("Логин пользователя: email = {email}")
    public static Response login(String email, String password) {
        return given()
                .header("Content-type", "application/json")
                .body(String.format("{\"email\": \"%s\", \"password\": \"%s\"}", email, password))
                .post(LOGIN_URL);
    }


    @Step("Удаление пользователя")
    public static Response deleteUser(String accessToken) {
        return given()
                .header("Authorization", accessToken)
                .delete(USER_URL);
    }

    @Step("Генерация уникального email")
    public static String generateUniqueEmail() {
        return "user_" + System.currentTimeMillis() + "@yandex.ru";
    }

    @Step("Обновление данных пользователя")
    public static Response updateUser(String accessToken, String email, String password, String name) {
        return given()
                .header("Content-type", "application/json")
                .header("Authorization", accessToken)
                .body(String.format("{\"email\": \"%s\", \"password\": \"%s\", \"name\": \"%s\"}",
                        email, password, name))
                .patch("/api/auth/user");
    }

}