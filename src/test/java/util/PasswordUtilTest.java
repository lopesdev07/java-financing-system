package util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilTest {
    @Test
    void mustValidateCorrectPasswordWithHashedPassword() {

        String plainPassword = "randomPassword123";
        String hashedPassword = PasswordUtil.plainToHash(plainPassword);


        boolean result = PasswordUtil.checkPassword(plainPassword, hashedPassword);


        assertTrue(result);
    }

    @Test
    void mustReturnFalseForWrongPassword() {
        String correctPassword = "randomPassword123";
        String wrongPassword = "wrongPassword456";
        String hashedPassword = PasswordUtil.plainToHash(correctPassword);

        boolean result = PasswordUtil.checkPassword(wrongPassword, hashedPassword);

        assertFalse(result);
    }

    @Test
    void mustGenerateNonNullHash() {
        String plainPassword = "randomPassword123";

        String hashedPassword = PasswordUtil.plainToHash(plainPassword);

        assertNotNull(hashedPassword);
    }

    @Test
    void hashMustBeDifferentFromPlainPassword() {
        String plainPassword = "randomPassword123";
        String hashedPassword = PasswordUtil.plainToHash(plainPassword);

        assertNotEquals(plainPassword, hashedPassword);
    }

    @Test
    void twoHashesOfSamePasswordMustBeDifferent() {
        String plainPassword = "randomPassword123";
        String hashedPassword = PasswordUtil.plainToHash(plainPassword);
        String hashedPassword2 = PasswordUtil.plainToHash(plainPassword);

        assertNotEquals(hashedPassword, hashedPassword2);
    }


}
