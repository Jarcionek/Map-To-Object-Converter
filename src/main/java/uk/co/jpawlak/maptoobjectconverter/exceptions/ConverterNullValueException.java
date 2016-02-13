package uk.co.jpawlak.maptoobjectconverter.exceptions;

/**
 * Thrown when value was null for {@link java.util.Optional non-optional} field.
 */
public class ConverterNullValueException extends ConverterException {

    public ConverterNullValueException(String message, Object... args) {
        super(String.format(message, args));
    }

}
