package hotelbookingsystem;

import hotel.model.User;

public class Session {

    private static User currentUser;

    private Session() {}

    public static User getCurrentUser()       { return currentUser; }
    public static void setCurrentUser(User u) { currentUser = u; }
    public static void logout()               { currentUser = null; }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
