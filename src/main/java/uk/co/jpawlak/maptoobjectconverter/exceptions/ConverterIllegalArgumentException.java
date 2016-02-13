package uk.co.jpawlak.maptoobjectconverter.exceptions;

/**
 * Thrown when any argument of public method does not meet the requirements, e.g.:
 * <ul>
 *     <li>argument is null</li>
 *     <li>type to register converter for is {@link java.util.Optional}</li>
 *     <li>target class to convert map to is invalid</li>
 * </ul>
 */
public class ConverterIllegalArgumentException extends ConverterException {

    public ConverterIllegalArgumentException(String message, Object... args) {
        super(String.format(message, args));
    }

}
