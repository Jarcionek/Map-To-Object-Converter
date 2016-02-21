package uk.co.jpawlak.maptoobjectconverter.exceptions;

/**
 * Thrown when the value is not assignable to field. In case of {@link java.util.Optional optional} fields,
 * it is thrown with type of the value is different than generic type parameter.
 */
public class ConverterTypeMismatchException extends ConverterException {

    public ConverterTypeMismatchException(String message, Object... args) {
        super(String.format(message, args));
    }

}
