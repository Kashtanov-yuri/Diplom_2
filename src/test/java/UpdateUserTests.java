import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

public class UpdateUserTests {

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
    @DisplayName("Изменение email с авторизацией")
    @Description("Проверка изменения email авторизованным пользователем")
    public void updateEmailWithAuthorizationShouldReturnSuccess() {
        createTestUser();
        String newEmail = "updated_" + System.currentTimeMillis() + "@yandex.ru";
        updateUserWithAuthorization(newEmail, userPassword, userName)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(newEmail.toLowerCase()));
    }

    @Test
    @DisplayName("Изменение имени с авторизацией")
    @Description("Проверка изменения имени авторизованным пользователем")
    public void updateNameWithAuthorizationShouldReturnSuccess() {
        createTestUser();
        String newName = "Updated Name";
        updateUserWithAuthorization(userEmail, userPassword, newName)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.name", equalTo(newName));
    }

    @Test
    @DisplayName("Изменение пароля с авторизацией")
    @Description("Проверка изменения пароля авторизованным пользователем")
    public void updatePasswordWithAuthorizationShouldReturnSuccess() {
        createTestUser();
        String newPassword = "newpassword123";
        updateUserWithAuthorization(userEmail, newPassword, userName)
                .then()
                .statusCode(200)
                .body("success", equalTo(true));
        UserClient.login(userEmail, newPassword)
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Изменение всех полей с авторизацией")
    @Description("Проверка изменения всех полей одновременно авторизованным пользователем")
    public void updateAllFieldsWithAuthorizationShouldReturnSuccess() {
        createTestUser();
        String newEmail = "completely_updated_" + System.currentTimeMillis() + "@yandex.ru";
        String newPassword = "completelynewpassword";
        String newName = "Completely Updated Name";

        updateUserWithAuthorization(newEmail, newPassword, newName)
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(newEmail.toLowerCase()))
                .body("user.name", equalTo(newName));
        UserClient.login(newEmail, newPassword)
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Изменение email без авторизации")
    @Description("Проверка ошибки при изменении email без авторизации")
    public void updateEmailWithoutAuthorizationShouldReturnError() {
        createTestUser();
        String newEmail = "unauthorized_update@" + System.currentTimeMillis() + ".ru";
        updateUserWithoutAuthorization(newEmail, userPassword, userName)
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Test
    @DisplayName("Изменение имени без авторизации")
    @Description("Проверка ошибки при изменении имени без авторизации")
    public void updateNameWithoutAuthorizationShouldReturnError() {
        createTestUser();
        String newName = "Unauthorized Name Update";
        updateUserWithoutAuthorization(userEmail, userPassword, newName)
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Test
    @DisplayName("Изменение пароля без авторизации")
    @Description("Проверка ошибки при изменении пароля без авторизации")
    public void updatePasswordWithoutAuthorizationShouldReturnError() {
        createTestUser();
        String newPassword = "unauthorizednewpass";
        updateUserWithoutAuthorization(userEmail, newPassword, userName)
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
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

    @Step("Обновление данных пользователя с авторизацией: email = {email}, name = {name}")
    private Response updateUserWithAuthorization(String email, String password, String name) {
        return UserClient.updateUser(accessToken, email, password, name);
    }

    @Step("Обновление данных пользователя без авторизации: email = {email}, name = {name}")
    private Response updateUserWithoutAuthorization(String email, String password, String name) {
        return UserClient.updateUser("", email, password, name);
    }
}
