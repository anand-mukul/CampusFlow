package com.campusflow.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    private static final int BCRYPT_COST = 10;

    private PasswordUtil() {}

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_COST));
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) return false;
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}

