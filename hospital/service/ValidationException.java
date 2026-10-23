
package hospital.service;

/**
 * Thrown when user input or a business rule is violated
 * (e.g. "slot already booked", "age must be a number").
 * The message is written to be shown directly to reception staff.
 */
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }
}
