
public class User {
    private boolean success;
    private String email;
    private String name;
    private String accessToken;
    private String refreshToken;


    public void setSuccess(boolean success) {
        this.success = success;
    }


    public void setEmail(String email) {
        this.email = email;
    }


    public void setName(String name) {
        this.name = name;
    }


    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }


    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public String toString() {
        return "User{success=" + success + ", email='" + email + "', name='" + name +
                "', accessToken='" + accessToken + "', refreshToken='" + refreshToken + "'}";
    }
}