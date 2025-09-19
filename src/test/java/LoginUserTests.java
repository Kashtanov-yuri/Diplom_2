import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.equalTo;

public class LoginUserTests {

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
    @DisplayName("Логин под существующим пользователем")
    @Description("Проверка успешного логина с валидными credentials")
    public void loginWithValidCredentialsShouldReturnSuccess() {
        createTestUser();
        loginAndVerifySuccess();
    }

    @Test
    @DisplayName("Логин с неверным email")
    @Description("Проверка ошибки при логине с несуществующим email")
    public void loginWithInvalidEmailShouldReturnError() {
        loginWithInvalidCredentialsAndVerifyError("nonexistent@yandex.ru", "password");
    }

    @Test
    @DisplayName("Логин с неверным паролем")
    @Description("Проверка ошибки при логине с неверным паролем")
    public void loginWithInvalidPasswordShouldReturnError() {
        createTestUser();
        loginWithInvalidCredentialsAndVerifyError(userEmail, "wrongpassword");
    }

    @Test
    @DisplayName("Логин с пустым email")
    @Description("Проверка ошибки при логине с пустым email")
    public void loginWithEmptyEmailShouldReturnError() {
        loginWithInvalidCredentialsAndVerifyError("", "password");
    }

    @Test
    @DisplayName("Логин с пустым паролем")
    @Description("Проверка ошибки при логине с пустым паролем")
    public void loginWithEmptyPasswordShouldReturnError() {
        createTestUser();
        loginWithInvalidCredentialsAndVerifyError(userEmail, "");
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

    @Step("Логин и проверка успешной аутентификации")
    private void loginAndVerifySuccess() {
        Response loginResponse = UserClient.login(userEmail, userPassword);
        loginResponse.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(userEmail.toLowerCase()))
                .body("user.name", equalTo(userName));
    }

    @Step("Попытка логина с неверными credentials: email = {email}")
    private void loginWithInvalidCredentialsAndVerifyError(String email, String password) {
        Response loginResponse = UserClient.login(email, password);
        loginResponse.then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }
}
