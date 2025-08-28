package main.java.nexus.validators;

public class ValidationResult {
    public final boolean ok;
    public final String message;

    private ValidationResult(boolean ok, String message) {
        this.ok = ok;
        this.message = message;
    }

    public static ValidationResult success() {
        return new ValidationResult(true, "OK");
    }

    public static ValidationResult fail(String msg) {
        return new ValidationResult(false, msg);
    }
}
