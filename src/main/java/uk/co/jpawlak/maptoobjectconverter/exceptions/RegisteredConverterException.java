package uk.co.jpawlak.maptoobjectconverter.exceptions;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * Thrown when registered converter:
 * <ul>
 *     <li>returns null for non-Optional field</li>
 *     <li>or throws exception</li>
 *     <li>or it returned value of type which is not assignable to field</li>
 * </ul>
 */
public class RegisteredConverterException extends ConverterException {

    private String extraMessage = "";

    public RegisteredConverterException(String message, Object... args) {
        super(String.format(message, args));
    }

    public RegisteredConverterException(Throwable cause) {
        super(cause);
    }

    public void setExtraMessage(Class<?> targetClass, Field field) {
        this.extraMessage = "" +
                "\n\t\tconverter registered for type: " + f(field).getTypeName() +
                "\n\t\ttarget class: " + targetClass.getTypeName() +
                "\n\t\tfield name: " + field.getName() +
                "\n\t\tfield type: " + field.getGenericType().getTypeName() +
                "";
    }

    private Type f(Field field) {
        // TODO: same check as in Converters, extract to utils? these are different packages...
        return field.getGenericType() instanceof ParameterizedType && ((ParameterizedType) field.getGenericType()).getRawType() == Optional.class ? ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0] : field.getGenericType();
    }

    @Override
    public String getMessage() {
        return super.getMessage() + extraMessage;
    }

}
