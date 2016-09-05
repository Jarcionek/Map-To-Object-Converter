package uk.co.jpawlak.maptoobjectconverter.exceptions;

/**
 * Thrown when converter is set to keys-case-insensitive mode and target class contains two fields for which
 * <code>fieldNameOne.equalsIgnoreCase(fieldNameTwo)</code> is true.
 */
public class ConverterFieldDuplicateException extends ConverterException {

    public ConverterFieldDuplicateException(String message, Object... args) {
        super(String.format(message, args));
    }

}
