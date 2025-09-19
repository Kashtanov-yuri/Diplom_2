
public class User {
    private boolean success;
    private String email;
    private String name;
    private String accessToken;
    private String refreshToken;



    @Override
    public String toString() {
        return "User{success=" + success + ", email='" + email + "', name='" + name +
                "', accessToken='" + accessToken + "', refreshToken='" + refreshToken + "'}";
    }
}