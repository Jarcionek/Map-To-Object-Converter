package uk.co.jpawlak.maptoobjectconverter.exceptions;

/**
 * Thrown when automatic conversion to enum fails. This can be caused by:
 * <ul>
 *     <li>{@link java.lang.Enum#valueOf(Class, String) valueOf(String)} throwing an exception</li>
 *     <li>or value being of different type than String</li>
 * </ul>
 * Both cases can be prevented by registering a converter for the enum.
 */
public class ConverterEnumCreationException extends ConverterException {

    public ConverterEnumCreationException(String message, Object... args) {
        super(String.format(message, args));
    }

}
