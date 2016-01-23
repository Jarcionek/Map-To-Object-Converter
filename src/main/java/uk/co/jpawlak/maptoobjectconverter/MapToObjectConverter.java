package uk.co.jpawlak.maptoobjectconverter;

import sun.reflect.ReflectionFactory;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;

public class MapToObjectConverter {

    private static final ReflectionFactory REFLECTION_FACTORY = ReflectionFactory.getReflectionFactory();

    public <T> T convert(Map<String, Object> map, Class<T> aClass) {
        if (aClass.isPrimitive()) {
            throw exception("Converting to unboxed primitives types is not supported, use boxed primitive type instead");
        }

        if (isBasicType(aClass)) {
            return singleBasicValue(map, aClass);
        }

        checkKeysEqualToFieldsNames(map.keySet(), aClass);
        checkOptionalFieldsForNullValues(map, aClass);

        T result = createInstance(aClass);

        setFields(map, aClass, result);

        return result;
    }

    private static boolean isBasicType(Class<?> aClass) {
        return aClass == String.class
                || aClass == Character.class
                || aClass == Boolean.class
                || aClass == Byte.class
                || aClass == Short.class
                || aClass == Integer.class
                || aClass == Long.class
                || aClass == Float.class
                || aClass == Double.class;
    }

    private static <T> T singleBasicValue(Map<String, Object> map, Class<T> aClass) {
        if (map.isEmpty()) {
            throw exception("Cannot convert empty map to single basic value of type '%s'", aClass.getName());
        }
        if (map.size() != 1) {
            throw exception("Cannot convert non-singleton map to single basic value of type '%s'. Keys found: '%s'", aClass.getName(), map.keySet().stream().collect(joining("', '")));
        }

        @SuppressWarnings("unchecked")
        T result = (T) map.values().stream().findFirst().get();

        if (!aClass.isInstance(result)) {
            throw exception("Cannot convert type '%s' to basic type '%s'", result.getClass().getName(), aClass.getName());
        }

        return result;
    }

    private static void checkKeysEqualToFieldsNames(Set<String> keys, Class<?> aClass) {
        Set<String> fieldsNames = fieldsNamesOf(aClass);

        Set<String> missingFields = keys.stream().filter(key -> !fieldsNames.contains(key)).collect(toCollection(LinkedHashSet::new));
        if (!missingFields.isEmpty()) {
            throw exception("No fields for keys: '%s'", missingFields.stream().collect(joining("', '")));
        }

        Set<String> missingValues = fieldsNames.stream().filter(fieldName -> !keys.contains(fieldName)).collect(toCollection(LinkedHashSet::new));
        if (!missingValues.isEmpty()) {
            throw exception("No values for fields: '%s'", missingValues.stream().collect(joining("', '")));
        }
    }

    private static void checkOptionalFieldsForNullValues(Map<String, Object> map, Class<?> aClass) {
        Set<String> fieldsNames = map.entrySet().stream()
                .filter(entry -> entry.getValue() == null && field(aClass, entry.getKey()).getType() != Optional.class)
                .map(Entry::getKey)
                .collect(toCollection(LinkedHashSet::new));

        if (!fieldsNames.isEmpty()) {
            throw exception("Null values require fields to be Optional. Null values for fields: '%s'", fieldsNames.stream().collect(joining("', '")));
        }
    }

    private static Set<String> fieldsNamesOf(Class<?> aClass) {
        return stream(aClass.getDeclaredFields())
                .map(Field::getName)
                .collect(toCollection(LinkedHashSet::new));
    }

    private static <T> T createInstance(Class<T> aClass) {
        try {
            Constructor<Object> objectNoArgConstructor = Object.class.getDeclaredConstructor();
            Constructor<?> constructor = REFLECTION_FACTORY.newConstructorForSerialization(aClass, objectNoArgConstructor);
            return aClass.cast(constructor.newInstance());
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> void setFields(Map<String, Object> map, Class<T> aClass, T result) {
        for (Entry<String, Object> entry : map.entrySet()) {
            Field field = field(aClass, entry.getKey());

            if (field.getType() == Optional.class) {
                setOptionalField(result, field, entry.getValue());
            } else {
                setField(result, field, entry.getValue());
            }
        }
    }

    private static Field field(Class<?> aClass, String fieldName) {
        try {
            return aClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setOptionalField(Object object, Field field, Object value) {
        if (value == null) {
            setField(object, field, Optional.empty());
        } else {
            Type genericType = ((ParameterizedTypeImpl) field.getGenericType()).getActualTypeArguments()[0];
            if (!(genericType instanceof Class<?>)) {
                throw exception("Wildcards are not supported. Field '%s' is 'Optional<%s>'", field.getName(), genericType);
            }

            if (value.getClass() != genericType) {
                throw exception("Cannot assign value of type 'Optional<%s>' to field '%s' of type 'Optional<%s>'", value.getClass().getName(), field.getName(), genericType.getTypeName());
            }

            setField(object, field, Optional.of(value));
        }
    }

    private static void setField(Object object, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(object, value);
        } catch (IllegalArgumentException e) {
            throw exception("Cannot assign value of type '%s' to field '%s' of type '%s'", value.getClass().getName(), field.getName(), field.getType().getName());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static IllegalArgumentException exception(String message, Object... args) {
        return new IllegalArgumentException(String.format(message, args));
    }

}
