package uk.co.jpawlak.maptoobjectconverter.exceptions;

/**
 * Thrown when converting fails for any reason which was not considered before.
 * <br>
 * Please report such cases at <a href="https://github.com/Jarcionek/Map-To-Object-Converter">https://github.com/Jarcionek/Map-To-Object-Converter</a>
 */
public class ConverterUnknownException extends ConverterException {

    public ConverterUnknownException(Throwable cause) {
        super("If you see this exception, it means that you have found a use case which was not considered before. Please report it at https://github.com/Jarcionek/Map-To-Object-Converter", cause);
    }

}
