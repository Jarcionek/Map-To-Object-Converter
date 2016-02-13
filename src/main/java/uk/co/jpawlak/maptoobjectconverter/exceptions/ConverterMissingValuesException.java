package uk.co.jpawlak.maptoobjectconverter.exceptions;

/**
 * Thrown when target class has a field for which there was no value in the map.
 */
public class ConverterMissingValuesException extends ConverterException {

    public ConverterMissingValuesException(String message, Object... args) {
        super(String.format(message, args));
    }

}
