package uk.co.jpawlak.maptoobjectconverter;

import sun.reflect.ReflectionFactory;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;

public class MapToObjectConverter {

    private static final ReflectionFactory REFLECTION_FACTORY = ReflectionFactory.getReflectionFactory();

    public <T> T convert(Map<String, Object> map, Class<T> aClass) {
        if (isSingleton(map) && typeMatches(aClass, map)) {
            return singleValueFrom(map);
        }

        checkKeysEqualToFieldsNames(map.keySet(), aClass);
        checkOptionalFieldsForNullValues(map, aClass);

        T result = createInstance(aClass);

        setFields(map, aClass, result);

        return result;
    }

    private static boolean isSingleton(Map<?, ?> map) {
        return map.size() == 1;
    }

    private static boolean typeMatches(Class<?> aClass, Map<String, ?> map) {
        return map.values().stream()
                .filter(value -> value != null)
                .findFirst()
                .map(value -> isAssignable(aClass, value.getClass()))
                .orElse(false);
    }

    private static boolean isAssignable(Class<?> aClass, Class<?> valueClass) {
        return aClass.isAssignableFrom(valueClass)
                || (aClass == char.class && valueClass == Character.class)
                || (aClass == boolean.class && valueClass == Boolean.class)
                || (aClass == byte.class && valueClass == Byte.class)
                || (aClass == short.class && valueClass == Short.class)
                || (aClass == int.class && valueClass == Integer.class)
                || (aClass == long.class && valueClass == Long.class)
                || (aClass == float.class && valueClass == Float.class)
                || (aClass == double.class && valueClass == Double.class);
    }

    @SuppressWarnings("unchecked")
    private static <T> T singleValueFrom(Map<String, Object> map) {
        return (T) map.values().stream().findFirst().get();
    }

    private static void checkKeysEqualToFieldsNames(Set<String> keys, Class<?> aClass) {
        Set<String> fieldsNames = fieldsOf(aClass)
                .map(Field::getName)
                .collect(toCollection(LinkedHashSet::new));

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
        Set<String> fieldsNames = fieldsOf(aClass)
                .filter(field -> field .getType() != Optional.class && map.get(field.getName()) == null)
                .map(Field::getName)
                .collect(toCollection(LinkedHashSet::new));

        if (!fieldsNames.isEmpty()) {
            throw exception("Null values require fields to be Optional. Null values for fields: '%s'", fieldsNames.stream().collect(joining("', '")));
        }
    }

    private static <T> T createInstance(Class<T> aClass) {
        try {
            if ((aClass.getModifiers() & Modifier.ABSTRACT) != 0) {
                throw exception("Cannot convert map to abstract class");
            }

            Constructor<Object> objectNoArgConstructor = Object.class.getDeclaredConstructor();
            Constructor<?> constructor = REFLECTION_FACTORY.newConstructorForSerialization(aClass, objectNoArgConstructor);
            return aClass.cast(constructor.newInstance());
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> void setFields(Map<String, Object> map, Class<T> aClass, T result) {
        fieldsOf(aClass).forEach(field -> {
            if (field.getType() == Optional.class) {
                setOptionalField(result, field, map.get(field.getName()));
            } else {
                setField(result, field, map.get(field.getName()));
            }
        });
    }

    private static Stream<Field> fieldsOf(Class<?> aClass) {
        Stream<Field> fields = Stream.empty();
        while (aClass != Object.class) {
            fields = Stream.concat(fields, Arrays.stream(aClass.getDeclaredFields()));
            aClass = aClass.getSuperclass();
        }
        return fields
                .filter(field -> (field.getModifiers() & Modifier.STATIC) == 0)
                .filter(field -> !field.isSynthetic());
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
