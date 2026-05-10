package utils;

import entities.User;

public class SessionManager {
    private static User currentUser;
    private SessionManager() {}
    public static void   setCurrentUser(User u) { currentUser = u; }
    public static User   getCurrentUser()        { return currentUser; }
    public static void   logout()                { currentUser = null; }
    public static boolean isLoggedIn()           { return currentUser != null; }
}
