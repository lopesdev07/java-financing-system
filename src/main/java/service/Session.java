package service;

public class Session {
    private static Integer userId;

    public static void login(int userId) {
        Session.userId = userId;
    }

    public static Integer getUserId() {
        return userId;
    }

    public static boolean isLoggedIn() {
        return userId != null;
    }

    public static void logout() {
        Session.userId = null;
    }
}