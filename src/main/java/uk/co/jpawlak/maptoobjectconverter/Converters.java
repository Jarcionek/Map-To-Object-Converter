package uk.co.jpawlak.maptoobjectconverter;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class Converters {

    private final Map<Type, SingleValueConverter<?>> converters = new HashMap<>();

    <T> void registerConverter(Class<T> aClass, SingleValueConverter<T> singleValueConverter) {
        if (aClass == null) {
            throw exception("Cannot register converter for null class.");
        }
        if (aClass == Optional.class) {
            throw exception("Cannot register convert for 'java.util.Optional'. Register converter for the type parameter instead.");
        }
        if (singleValueConverter == null) {
            throw exception("Registered converter cannot be null.");
        }
        converters.put(aClass, new ExceptionWrappingSingleValueConverter<>(singleValueConverter));
    }

    boolean hasConverterFor(Type type) {
        return converters.containsKey(type);
    }

    SingleValueConverter<?> getConverterFor(Type type, String fieldName) {
        if (type == Optional.class) {
            throw exception("Raw types are not supported. Field '%s' is 'Optional'", fieldName);
        }

        if (type instanceof ParameterizedTypeImpl && ((ParameterizedTypeImpl) type).getRawType() == Optional.class) {
            Type parameterType = ((ParameterizedTypeImpl) type).getActualTypeArguments()[0];
            if (!(parameterType instanceof Class<?>)) {
                throw exception("Wildcards are not supported. Field '%s' is 'Optional<%s>'", fieldName, parameterType);
            }
            return optionalValueConverter(type, fieldName);
        }

        if (converters.containsKey(type)) {
            return converters.get(type);
        }

        if (type instanceof Class<?> && ((Class<?>) type).isEnum()) {
            return value -> asEnum(((Class<?>) type), value);
        }

        return value -> value;
    }

    private SingleValueConverter<?> optionalValueConverter(Type type, String fieldName) {
        return value -> {
            Type parameterType = ((ParameterizedTypeImpl) type).getActualTypeArguments()[0];

            if (this.hasConverterFor(parameterType)) {
                SingleValueConverter<?> converter = this.getConverterFor(parameterType, fieldName);
                Object convertedValue = converter.convert(value);
                if (convertedValue != null && convertedValue.getClass() != parameterType) {
                    throw new RegisteredConverterException(String.format("Cannot assign value of type 'Optional<%s>' returned by registered converter to field '%s' of type 'Optional<%s>'", convertedValue.getClass().getName(), fieldName, parameterType.getTypeName()));
                }
                return Optional.ofNullable(convertedValue);
            }

            if (value == null) {
                return Optional.empty();
            }

            if (((Class<?>) parameterType).isEnum()) {
                return Optional.of(asEnum((Class<?>) parameterType, value));
            }

            if (value.getClass() != parameterType) {
                throw exception("Cannot assign value of type 'Optional<%s>' to field '%s' of type 'Optional<%s>'", value.getClass().getName(), fieldName, parameterType.getTypeName());
            }

            return Optional.of(value);
        };
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
