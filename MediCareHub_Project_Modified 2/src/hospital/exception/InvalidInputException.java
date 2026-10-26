package hospital.exception;

/**
 * Custom exception class to handle invalid input scenarios
 * such as invalid age, empty fields, or duplicate IDs.
 * All logic in this class is my own original work.
 */
public class InvalidInputException extends Exception {

    // Constructor that passes the error message to the parent Exception class
    public InvalidInputException(String message) {
        super(message);
    }
}