package uk.co.jpawlak.maptoobjectconverter;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterEnumCreationException;
import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterIllegalArgumentException;
import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterTypeMismatchException;
import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterUnknownException;
import uk.co.jpawlak.maptoobjectconverter.exceptions.RegisteredConverterException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class Converters {

    private final Map<Type, SingleValueConverter<?>> converters = new HashMap<>();

    void registerConverter(Type type, SingleValueConverter<?> singleValueConverter) {
        if (type == null) {
            throw new ConverterIllegalArgumentException("Cannot register converter for null class.");
        }
        if (type == Optional.class) {
            throw new ConverterIllegalArgumentException("Cannot register convert for 'java.util.Optional'. Register converter for the type parameter instead.");
        }
        if (singleValueConverter == null) {
            throw new ConverterIllegalArgumentException("Registered converter cannot be null.");
        }
        converters.put(type, new ExceptionWrappingSingleValueConverter<>(singleValueConverter));
    }

    boolean hasConverterFor(Type type) {
        return converters.containsKey(type);
    }

    SingleValueConverter<?> getConverterFor(Type type, String fieldName) {
        if (type == Optional.class) {
            throw new ConverterIllegalArgumentException("Raw types are not supported. Field '%s' is 'Optional'", fieldName);
        }

        if (type instanceof ParameterizedTypeImpl && ((ParameterizedTypeImpl) type).getRawType() == Optional.class) {
            Type parameterType = ((ParameterizedTypeImpl) type).getActualTypeArguments()[0];
            if (!(parameterType instanceof Class<?>)) {
                throw new ConverterIllegalArgumentException("Wildcards are not supported. Field '%s' is 'Optional<%s>'", fieldName, parameterType);
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

            SingleValueConverter<?> converter = this.getConverterFor(parameterType, fieldName);
            Object convertedValue = converter.convert(value);

            if (convertedValue != null && convertedValue.getClass() != parameterType) {
                if (this.hasConverterFor(parameterType)) {
                    throw new RegisteredConverterException("Cannot assign value of type 'Optional<%s>' returned by registered converter to field '%s' of type 'Optional<%s>'", convertedValue.getClass().getTypeName(), fieldName, parameterType.getTypeName());
                } else {
                    throw new ConverterTypeMismatchException("Cannot assign value of type 'Optional<%s>' to field '%s' of type 'Optional<%s>'", value.getClass().getTypeName(), fieldName, parameterType.getTypeName());
                }
            }
            return Optional.ofNullable(convertedValue);
        };
    }

    @SuppressWarnings("unchecked")
    private static <E> E asEnum(Class<E> enumClass, Object value) {
        if (value == null) {
            return null;
        }
        try {
            return (E) enumClass.getDeclaredMethod("valueOf", String.class).invoke(null, value);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            if (e instanceof InvocationTargetException && e.getCause() instanceof IllegalArgumentException) {
                throw new ConverterEnumCreationException("'%s' does not have an enum named '%s'", enumClass.getTypeName(), value);
            }
            if (e instanceof IllegalArgumentException) {
                throw new ConverterEnumCreationException("Cannot convert value of type '%s' to enum", value.getClass().getTypeName());
            }
            throw new ConverterUnknownException(e);
        }
    }

}
