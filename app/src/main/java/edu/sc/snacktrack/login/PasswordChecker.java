package edu.sc.snacktrack.login;

/**
 * This class is a tool for checking the strength of passwords. A PasswordChecker holds the
 * requirements of a password and can check which of those requirements a password meets.
 */
public class PasswordChecker {

    public static final int DEFAULT_MINIMUM_LENGTH = 8;
    public static final boolean DEFAULT_REQUIRE_LOWERCASE  = true;
    public static final boolean DEFAULT_REQUIRE_UPPERCASE = true;
    public static final boolean DEFAULT_REQUIRE_NUMBERS = true;
    public static final boolean DEFAULT_REQUIRE_SPECIAL_CHARS = false;

    private static final String numbers = "0123456789";
    private static final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private int minimumLength;
    private boolean requireLowercase;
    private boolean requireUppercase;
    private boolean requireNumbers;
    private boolean requireSpecial;

    /**
     * Create a new password checker using the default values.
     */
    public PasswordChecker(){
        minimumLength = DEFAULT_MINIMUM_LENGTH;
        requireLowercase = DEFAULT_REQUIRE_LOWERCASE;
        requireUppercase = DEFAULT_REQUIRE_UPPERCASE;
        requireNumbers = DEFAULT_REQUIRE_NUMBERS;
        requireSpecial = DEFAULT_REQUIRE_SPECIAL_CHARS;
    }

    // Standard getters and setters
    public int getMinimumLength() {
        return minimumLength;
    }
    public void setMinimumLength(int minimumLength) {
        this.minimumLength = minimumLength;
    }
    public boolean isRequireSpecial() {
        return requireSpecial;
    }
    public void setRequireSpecial(boolean requireSpecial) {
        this.requireSpecial = requireSpecial;
    }
    public boolean isRequireNumbers() {
        return requireNumbers;
    }
    public void setRequireNumbers(boolean requireNumbers) {
        this.requireNumbers = requireNumbers;
    }
    public boolean isRequireUppercase() {
        return requireUppercase;
    }
    public void setRequireUppercase(boolean requireUppercase) {
        this.requireUppercase = requireUppercase;
    }
    public boolean isRequireLowercase() {
        return requireLowercase;
    }
    public void setRequireLowercase(boolean requireLowercase) {
        this.requireLowercase = requireLowercase;
    }

    /**
     * Parses a password string and populates the fields specified in PasswordChecker.CheckResult.
     *
     * @param password The password to check
     * @return The CheckResult
     */
    public static CheckResult checkPassword(String password){
        CheckResult result = new CheckResult();

        String passwordLowercase = password.toLowerCase();
        String passwordUppercase = password.toUpperCase();

        result.length = password.length();
        result.hasLowerCase = !password.equals(passwordUppercase);
        result.hasUpperCase = !password.equals(passwordLowercase);

        for(char c1 : passwordUppercase.toCharArray()){
            String str = Character.toString(c1);
            if(alphabet.contains(str)){
                result.hasLetters = true;
            } else if(numbers.contains(str)){
                result.hasNumbers = true;
            } else{
                result.hasSpecialCharacters = true;
            }

            // Break if all characters of interest have been found
            if(result.hasLetters && result.hasSpecialCharacters && result.hasNumbers){
                break;
            }
        }

        return result;
    }

    /**
     * Checks if a password meets the requirements of this password checker.
     *
     * @param password The password to check
     * @return true if the password meets requirements. false otherwise.
     */
    public boolean meetsRequirements(String password){
        CheckResult checkResult = checkPassword(password);
        if(checkResult.length() < minimumLength){
            return false;
        } else if(requireLowercase && !checkResult.hasLowerCase()){
            return false;
        } else if(requireUppercase && !checkResult.hasUpperCase()){
            return false;
        } else if(requireNumbers && !checkResult.hasNumbers()){
            return false;
        } else if(requireSpecial && !checkResult.hasSpecialCharacters()){
            return false;
        } else{
            return true;
        }
    }

    /**
     * This class holds the results of checking the strength of a password.
     */
    public static class CheckResult{

        private boolean hasLowerCase;
        private boolean hasUpperCase;
        private boolean hasLetters;
        private boolean hasNumbers;
        private boolean hasSpecialCharacters;
        private int length;

        public CheckResult(){
            hasLowerCase = false;
            hasUpperCase = false;
            hasNumbers = false;
            hasSpecialCharacters = false;
            length = 0;
        }

        // Standard getters
        public boolean hasLowerCase(){
            return hasLetters && hasLowerCase;
        }
        public boolean hasUpperCase(){
            return hasLetters && hasUpperCase;
        }
        public boolean hasMixedCase(){
            return hasUpperCase() && hasLowerCase();
        }
        public boolean hasLetters() {
            return hasLetters;
        }
        public boolean hasNumbers(){
            return hasNumbers;
        }
        public boolean hasSpecialCharacters(){
            return hasSpecialCharacters;
        }
        public int length(){
            return length;
        }
    }
}
