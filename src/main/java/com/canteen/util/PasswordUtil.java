package com.canteen.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Password hashing with SHA-256 + per-user random salt.
 *
 * NEVER store plaintext passwords. For each user we generate a random salt,
 * store (salt, SHA-256(salt + password)), and on login recompute the hash
 * with the stored salt and compare. The salt defeats rainbow-table attacks:
 * two users with the same password end up with different hashes.
 *
 * (Viva note: SHA-256+salt is fine for a college project. Real systems prefer
 *  bcrypt/Argon2 because they are deliberately slow, which blunts brute force.)
 */
public final class PasswordUtil {

    private PasswordUtil() {
        // utility class - no instances
    }

    /** Generates a random 16-byte salt, returned as lowercase hex. */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return toHex(salt);
    }

    /** Returns SHA-256(salt + password) as lowercase hex. */
    public static String hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // Salt first, then password - order just has to be consistent.
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return toHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to exist on every JVM; this is defensive.
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** True when the supplied password matches the stored (salt, hash) pair. */
    public static boolean verify(String password, String salt, String expectedHash) {
        String actual = hash(password, salt);
        // MessageDigest.isEqual compares in constant time (timing-attack hygiene).
        return MessageDigest.isEqual(actual.getBytes(StandardCharsets.UTF_8),
                                     expectedHash.getBytes(StandardCharsets.UTF_8));
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
