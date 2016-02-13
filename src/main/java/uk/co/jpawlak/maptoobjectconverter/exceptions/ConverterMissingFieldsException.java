package uk.co.jpawlak.maptoobjectconverter.exceptions;

/**
 * Thrown when target class does not have a field for the value found in the map.
 */
public class ConverterMissingFieldsException extends ConverterException {

    public ConverterMissingFieldsException(String message, Object... args) {
        super(String.format(message, args));
    }

}
