package it.unipi.aide.utilities;

public class Validation {

    public static boolean validatePassword(String password, int length) {
        final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        if (password.length() < length) {
            return false;
        }
        for (int i = 0; i < password.length(); i++) {
            if (!characters.contains(String.valueOf(password.charAt(i)))) {
                return false;
            }
        }
        return true;
    }
}
