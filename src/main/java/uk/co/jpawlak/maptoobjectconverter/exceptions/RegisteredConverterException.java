package uk.co.jpawlak.maptoobjectconverter.exceptions;

/**
 * Thrown when registered converter:
 * <ul>
 *     <li>returns null for non-Optional field</li>
 *     <li>or throws exception</li>
 *     <li>or it returned value of type which is not assignable to field</li>
 * </ul>
 */
public class RegisteredConverterException extends ConverterException {

    public RegisteredConverterException(String message, Object... args) {
        super(String.format(message, args));
    }

    public RegisteredConverterException(Throwable cause) {
        super(cause);
    }

}
