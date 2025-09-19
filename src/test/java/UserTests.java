import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit5.AllureJunit5;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.Matchers.equalTo;

@ExtendWith(AllureJunit5.class)
public class UserTests {

    private String userEmail;
    private String userPassword;
    private String userName;
    private String accessToken;

    @BeforeEach
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
    @Description("Логин под существующим пользователем")
    public void loginWithExistingUserShouldReturnSuccess() {
        // Создаем пользователя
        userEmail = UserClient.generateUniqueEmail();
        userPassword = "password";
        userName = "Username";

        UserClient.createUser(userEmail, userPassword, userName);

        // Логинимся и получаем токен
        Response loginResponse = UserClient.login(userEmail, userPassword);
        accessToken = loginResponse.then()
                .extract()
                .path("accessToken");

        // Проверяем успешный логин (оставляем как было)
        loginResponse.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(userEmail.toLowerCase()))
                .body("user.name", equalTo(userName));
    }

    @Test
    @Description("Логин с неверным email")
    public void loginWithWrongEmailShouldReturnError() {
        loginWithInvalidCredentialsAndVerifyError("wrong@yandex.ru", "password");
    }

    @Test
    @Description("Логин с неверным паролем")
    public void loginWithWrongPasswordShouldReturnError() {
        // Создаем пользователя
        userEmail = UserClient.generateUniqueEmail();
        userPassword = "correctpassword";
        userName = "Username";

        UserClient.createUser(userEmail, userPassword, userName);

        // Получаем токен для очистки после теста
        Response loginResponse = UserClient.login(userEmail, userPassword);
        accessToken = loginResponse.then()
                .extract()
                .path("accessToken");

        // Пытаемся логиниться с неправильным паролем
        loginWithInvalidCredentialsAndVerifyError(userEmail, "wrongpassword");
    }

    @Test
    @Description("Создание дубликата пользователя")
    public void createDuplicateUserShouldReturnError() {
        userEmail = UserClient.generateUniqueEmail();
        userPassword = "password";
        userName = "Username";

        // Первое создание - успешно
        Response firstResponse = UserClient.createUser(userEmail, userPassword, userName);
        accessToken = firstResponse.then()
                .extract()
                .path("accessToken");

        firstResponse.then()
                .statusCode(200);

        // Второе создание - ошибка
        UserClient.createUser(userEmail, userPassword, userName)
                .then()
                .statusCode(403);
    }

    @Step("Попытка логина с неверными credentials: email = {email}, password = {password}")
    private void loginWithInvalidCredentialsAndVerifyError(String email, String password) {
        UserClient.login(email, password)
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }
}