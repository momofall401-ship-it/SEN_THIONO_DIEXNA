package sn.senthioonodiexna.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    private static final Pattern SENEGAL_PHONE_PATTERN = Pattern.compile(
            "^(\\+221\\s?)?(7[05678]\\d{7}|33\\d{7})$"
    );

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isBlank()) return true; // Phone is optional in some places
        String sanitized = phone.replaceAll("\\s+", "");
        return SENEGAL_PHONE_PATTERN.matcher(sanitized).matches();
    }

    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isValidPassword(String password) {
        // At least 6 characters
        return password != null && password.length() >= 6;
    }
}
