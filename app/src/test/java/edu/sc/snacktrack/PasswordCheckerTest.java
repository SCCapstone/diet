package edu.sc.snacktrack;

import org.junit.Test;

import static org.junit.Assert.*;

import edu.sc.snacktrack.login.PasswordChecker;

/**
 * Unit tests for password checker.
 */
public class PasswordCheckerTest {

    @Test
    public void testMeetsRequirements(){
        PasswordChecker defaultChecker = new PasswordChecker();

        // Too few characters (minimum is 8)
        String testPassword1 = "Foster1";
        boolean expectedResult1 = false;

        // No capital characters
        String testPassword2 = "foster12";
        boolean expectedResult2 = false;

        // No lowercase characters
        String testPassword3 = "FOSTER12";
        boolean expectedResult3 = false;

        // No numbers
        String testPassword4 = "IAmNotValid";
        boolean expectedResult4 = false;

        // Empty string
        String testPassword5 = "";
        boolean expectedResult5 = false;

        // Finally, a valid password
        String testPassword6 = "IAmValid1337";
        boolean expectedResult6 = true;

        assertEquals(defaultChecker.meetsRequirements(testPassword1), expectedResult1);
        assertEquals(defaultChecker.meetsRequirements(testPassword2), expectedResult2);
        assertEquals(defaultChecker.meetsRequirements(testPassword3), expectedResult3);
        assertEquals(defaultChecker.meetsRequirements(testPassword4), expectedResult4);
        assertEquals(defaultChecker.meetsRequirements(testPassword5), expectedResult5);
        assertEquals(defaultChecker.meetsRequirements(testPassword6), expectedResult6);
    }

    @Test
    public void testCheckResult(){
        // Mixed case, no numbers
        String testPassword1 = "Foster";

        // No capital letters
        String testPassword2 = "foster12";

        // No lowercase letters
        String testPassword3 = "FOSTER12";

        // No letters, has special characters
        String testPassword4 = "2^31-1";

        // Check results for password 1
        PasswordChecker.CheckResult result1 = PasswordChecker.checkPassword(testPassword1);
        assertTrue(result1.hasLetters());
        assertTrue(result1.hasLowerCase());
        assertTrue(result1.hasUpperCase());
        assertFalse(result1.hasNumbers());
        assertFalse(result1.hasSpecialCharacters());
        assertEquals(result1.length(), testPassword1.length());

        // Check results for password 2
        PasswordChecker.CheckResult result2 = PasswordChecker.checkPassword(testPassword2);
        assertTrue(result2.hasLetters());
        assertTrue(result2.hasLowerCase());
        assertFalse(result2.hasUpperCase());
        assertTrue(result2.hasNumbers());
        assertFalse(result2.hasSpecialCharacters());
        assertEquals(result2.length(), testPassword2.length());

        // Check results for password 3
        PasswordChecker.CheckResult result3 = PasswordChecker.checkPassword(testPassword3);
        assertTrue(result3.hasLetters());
        assertFalse(result3.hasLowerCase());
        assertTrue(result3.hasUpperCase());
        assertTrue(result3.hasNumbers());
        assertFalse(result3.hasSpecialCharacters());
        assertEquals(result3.length(), testPassword3.length());

        // Check results for password 4
        PasswordChecker.CheckResult result4 = PasswordChecker.checkPassword(testPassword4);
        assertFalse(result4.hasLetters());
        assertFalse(result4.hasLowerCase());
        assertFalse(result4.hasUpperCase());
        assertTrue(result4.hasNumbers());
        assertTrue(result4.hasSpecialCharacters());
        assertEquals(result4.length(), testPassword4.length());
    }
}
