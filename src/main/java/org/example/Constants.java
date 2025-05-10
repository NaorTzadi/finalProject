package org.example;

public class Constants {
    public static int BACKEND_PORT = Integer.parseInt(getEnv("BACKEND_PORT"));
    public static final String FRONTEND_PATH = getEnv("FRONTEND_PATH");
    public static final String USER_SESSION_SECRET_KEY = getEnv("USER_SESSION_SECRET_KEY");
    private static String getEnv(String key) {
        String val = System.getenv(key);
        return val != null ? val : System.getProperty(key);
    }

    public static final String[] ALLOWED_HTTP_METHODS = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"};
    public static final String[] ALLOWED_HTTP_HEADERS = {"Authorization", "Content-Type","SessionToken"};

    public final static String USERS_TABLE="users";
    public final static String UNVERIFIED_USERS_TABLE="unverified_users";
    public final static String USER_SETTINGS_TABLE="user_settings";
    public final static String FAST_ACCESS_TOKENS_TABLE="fast_access_tokens";
    public final static String FAILED_LOGIN_ATTEMPTS_TABLE="failed_login_attempts";
    public final static String RESET_PASSWORD_TOKENS_TABLE="fast_access_tokens";


}
