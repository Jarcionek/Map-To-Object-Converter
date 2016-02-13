package uk.co.jpawlak.maptoobjectconverter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class Converters {

    private final Map<Class<?>, SingleValueConverter<?>> converters = new HashMap<>();

    <T> void registerConverter(Class<T> aClass, SingleValueConverter<T> singleValueConverter) {
        if (aClass == Optional.class) {
            throw new IllegalArgumentException(String.format("Cannot register convert for '%s'. Register converter for the type parameter instead.", Optional.class.getName()));
        }
        converters.put(aClass, singleValueConverter);
    }

    boolean hasConverterFor(Class<?> aClass) {
        return converters.containsKey(aClass);
    }

    Object convert(Object value, Class<?> type) {
        return convertedValue(converters.get(type), value);
    }

    private static Object convertedValue(SingleValueConverter<?> converter, Object value) {
        try {
            return converter.convert(value);
        } catch (Exception ex) {
            throw new RegisteredConverterException(ex);
        }
    }

}
