package uk.co.jpawlak.maptoobjectconverter;

import sun.reflect.ReflectionFactory;
import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterTypeMismatchException;
import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterUnknownException;
import uk.co.jpawlak.maptoobjectconverter.exceptions.RegisteredConverterException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

import static uk.co.jpawlak.maptoobjectconverter.Utils.fieldsOf;

class ObjectCreator {

    private static final ReflectionFactory REFLECTION_FACTORY = ReflectionFactory.getReflectionFactory();

    private final Converters converters;

    ObjectCreator(Converters converters) {
        this.converters = converters;
    }

    <T> T convertMapToObject(Map<String, Object> map, Class<T> targetClass) {
        T result = createInstance(targetClass);

        setFields(map, targetClass, result);

        return result;
    }

    private static <T> T createInstance(Class<T> targetClass) {
        try {
            Constructor<Object> objectNoArgConstructor = Object.class.getDeclaredConstructor();
            Constructor<?> constructor = REFLECTION_FACTORY.newConstructorForSerialization(targetClass, objectNoArgConstructor);
            return targetClass.cast(constructor.newInstance());
        } catch (Exception e) {
            throw new ConverterUnknownException(e);
        }
    }

    private <T> void setFields(Map<String, Object> map, Class<T> targetClass, T result) {
        fieldsOf(targetClass).forEach(field -> {
            SingleValueConverter<?> converter = converters.getConverterFor(field.getGenericType(), field.getName());
            Object value = map.get(field.getName());
            Object convertedValue = converter.convert(value);
            if (convertedValue == null) {
                throw new RegisteredConverterException("Null values require fields to be Optional. Registered converter for type '%s' returned null.", field.getType().getTypeName());
            }
            setField(result, field, convertedValue);
        });
    }

    private static void setField(Object object, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(object, value);
        } catch (IllegalArgumentException e) {
            throw new ConverterTypeMismatchException("Cannot assign value of type '%s' to field '%s' of type '%s'.", value.getClass().getTypeName(), field.getName(), field.getType().getTypeName());
        } catch (IllegalAccessException e) {
            throw new ConverterUnknownException(e);
        }
    }

}
