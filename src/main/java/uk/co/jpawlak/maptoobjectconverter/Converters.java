package uk.co.jpawlak.maptoobjectconverter;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class Converters {

    private final Map<Class<?>, SingleValueConverter<?>> converters = new HashMap<>();

    <T> void registerConverter(Class<T> aClass, SingleValueConverter<T> singleValueConverter) {
        if (aClass == null) {
            throw new IllegalArgumentException("Cannot register converter for null class.");
        }
        if (aClass == Optional.class) {
            throw new IllegalArgumentException("Cannot register convert for 'java.util.Optional'. Register converter for the type parameter instead.");
        }
        if (singleValueConverter == null) {
            throw new IllegalArgumentException("Registered converter cannot be null.");
        }
        converters.put(aClass, new ExceptionWrappingSingleValueConverter<>(singleValueConverter));
    }

    boolean hasConverterFor(Class<?> aClass) {
        return converters.containsKey(aClass);
    }

    SingleValueConverter<?> getConverterFor(Class<?> aClass) {
        if (converters.containsKey(aClass)) {
            return converters.get(aClass);
        }
        if (aClass.isEnum()) {
            return value -> asEnum(aClass, value);
        }
        return value -> value;
    }

    @SuppressWarnings("unchecked")
    private static <E> E asEnum(Class<E> enumClass, Object value) {
        try {
            return (E) enumClass.getDeclaredMethod("valueOf", String.class).invoke(null, value);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            if (e instanceof InvocationTargetException && e.getCause() instanceof IllegalArgumentException) {
                throw exception("'%s' does not have an enum named '%s'", enumClass.getName(), value);
            }
            if (e instanceof IllegalArgumentException) {
                throw exception("Cannot convert value of type '%s' to enum", value.getClass().getName());
            }
            throw new RuntimeException(e);
        }
    }

    private static IllegalArgumentException exception(String message, Object... args) {
        return new IllegalArgumentException(String.format(message, args));
    }
}
