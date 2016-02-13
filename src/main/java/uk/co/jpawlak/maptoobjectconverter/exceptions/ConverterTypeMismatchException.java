package uk.co.jpawlak.maptoobjectconverter.exceptions;

/**
 * Thrown when the type of the value is different than type of the field.
 */
public class ConverterTypeMismatchException extends ConverterException {

    public ConverterTypeMismatchException(String message, Object... args) {
        super(String.format(message, args));
    }

}
