package uk.co.jpawlak.maptoobjectconverter;

/**
 * Thrown when registered converter:
 * - returns null for non-Optional field
 * - throws exception
 */
public class RegisteredConverterException extends RuntimeException {

    public RegisteredConverterException(String message) {
        super(message);
    }

}
