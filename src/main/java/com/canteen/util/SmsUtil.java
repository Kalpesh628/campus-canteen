package com.canteen.util;

/**
 * Phone (SMS) one-time codes.
 *
 * DEMO MODE: no SMS gateway is configured, so this class never actually sends
 * a text message - the servlets show the code on screen instead (same pattern
 * the email flow used before it). This keeps registration and password-reset
 * fully working for the college demo without spending money on SMS.
 *
 * Going live with real SMS later means: sign up with an SMS gateway
 * (Twilio, MSG91, etc.), put the credentials in environment variables,
 * implement {@link #sendSms(String, String)}, and flip {@link #isConfigured()}
 * to true. The servlets already branch on isConfigured().
 *
 * (Viva note: real SMS costs money per message and needs a gateway account -
 * that's why demo projects show the code on screen instead.)
 */
public final class SmsUtil {

    private SmsUtil() {
        // utility class - no instances
    }

    /** True when a real SMS gateway is configured. Always false in demo mode. */
    public static boolean isConfigured() {
        String sid = System.getenv("SMS_API_KEY");
        return sid != null && !sid.isBlank();
    }

    /**
     * Normalizes an Indian mobile number to 10 digits.
     * Accepts "9876543210", "+91 98765 43210", "91-9876543210", etc.
     * Returns null when the input cannot be normalized.
     */
    public static String normalizePhone(String raw) {
        if (raw == null) return null;
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.startsWith("91") && digits.length() == 12) {
            digits = digits.substring(2);
        }
        if (digits.length() == 11 && digits.startsWith("0")) {
            digits = digits.substring(1);
        }
        return digits.length() == 10 ? digits : null;
    }

    /** Indian mobile numbers start with 6-9. */
    public static boolean isValidIndianMobile(String normalized) {
        return normalized != null && normalized.matches("[6-9][0-9]{9}");
    }

    /** Masks a number for display: "9876543210" -> "+91 98765 **210". */
    public static String maskPhone(String normalized) {
        if (normalized == null || normalized.length() != 10) return "your phone";
        return "+91 " + normalized.substring(0, 5) + " **"
                + normalized.substring(7);
    }

    /**
     * Sends a registration verification code by SMS.
     * Demo mode: returns false without sending; the caller shows the code
     * on screen instead.
     */
    public static boolean sendVerificationCode(String phone, String code) {
        return sendSms(phone, "Your Campus Canteen verification code is " + code
                + ". It expires in 15 minutes.");
    }

    /**
     * Sends a password-reset code by SMS.
     * Demo mode: returns false without sending; the caller shows the code
     * on screen instead.
     */
    public static boolean sendPasswordResetCode(String phone, String code) {
        return sendSms(phone, "Your Campus Canteen password-reset code is " + code
                + ". It expires in 15 minutes. If you didn't ask for this, ignore it.");
    }

    /** Real gateway hook - implemented when SMS_API_KEY is configured. */
    private static boolean sendSms(String phone, String text) {
        if (!isConfigured()) return false;
        // TODO: plug in Twilio / MSG91 here with the SMS_API_KEY env var.
        return false;
    }
}
