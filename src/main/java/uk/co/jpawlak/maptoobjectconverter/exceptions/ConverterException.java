package uk.co.jpawlak.maptoobjectconverter.exceptions;

/**
 * Superclass for all converter exceptions.
 *
 * @see ConverterEnumCreationException
 * @see ConverterIllegalArgumentException
 * @see ConverterMissingFieldsException
 * @see ConverterMissingValuesException
 * @see ConverterNullValueException
 * @see ConverterTypeMismatchException
 * @see ConverterUnknownException
 * @see RegisteredConverterException
 */
public abstract class ConverterException extends RuntimeException {

    public ConverterException(String message) {
        super(message);
    }

    public ConverterException(Throwable cause) {
        super(cause);
    }

    public ConverterException(String message, Throwable cause) {
        super(message, cause);
    }

}
