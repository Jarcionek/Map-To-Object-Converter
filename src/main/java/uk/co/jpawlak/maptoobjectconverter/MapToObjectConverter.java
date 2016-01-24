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

    public <T> T convert(Map<String, Object> map, Class<T> targetClass) {
        checkParameters(map, targetClass);
        checkKeysEqualToFieldsNames(map.keySet(), targetClass);
        checkOptionalFieldsForNullValues(map, targetClass);

        T result = createInstance(targetClass);

        setFields(map, targetClass, result);

        return result;
    }

    private static void checkParameters(Map<?, ?> map, Class<?> targetClass) {
        if (map == null) {
            throw exception("Map cannot be null");
        }
        if (targetClass == null) {
            throw exception("Target class cannot be null");
        }
        if (targetClass.isPrimitive()) {
            throw exception("Cannot convert map to primitive type. Use boxed primitive instead");
        }
        if (targetClass.isEnum()) {
            throw exception("Cannot convert map to enum");
        }
        if (targetClass.isAnnotation()) {
            throw exception("Cannot convert map to annotation");
        }
        if (targetClass.isInterface()) {
            throw exception("Cannot convert map to interface");
        }
        if ((targetClass.getModifiers() & Modifier.ABSTRACT) != 0) {
            throw exception("Cannot convert map to abstract class");
        }
    }

    private static void checkKeysEqualToFieldsNames(Set<String> keys, Class<?> targetClass) {
        Set<String> fieldsNames = fieldsOf(targetClass)
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

    private static void checkOptionalFieldsForNullValues(Map<String, Object> map, Class<?> targetClass) {
        Set<String> fieldsNames = fieldsOf(targetClass)
                .filter(field -> field .getType() != Optional.class && map.get(field.getName()) == null)
                .map(Field::getName)
                .collect(toCollection(LinkedHashSet::new));

        if (!fieldsNames.isEmpty()) {
            throw exception("Null values require fields to be Optional. Null values for fields: '%s'", fieldsNames.stream().collect(joining("', '")));
        }
    }

    private static <T> T createInstance(Class<T> targetClass) {
        try {
            Constructor<Object> objectNoArgConstructor = Object.class.getDeclaredConstructor();
            Constructor<?> constructor = REFLECTION_FACTORY.newConstructorForSerialization(targetClass, objectNoArgConstructor);
            return targetClass.cast(constructor.newInstance());
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> void setFields(Map<String, Object> map, Class<T> targetClass, T result) {
        fieldsOf(targetClass).forEach(field -> {
            if (field.getType() == Optional.class) {
                setOptionalField(result, field, map.get(field.getName()));
            } else {
                if (field.getType().isEnum()) {
                    setField(result, field, asEnum(field.getType(), map.get(field.getName())));
                } else {
                    setField(result, field, map.get(field.getName()));
                }
            }
        });
    }

    private static void setOptionalField(Object object, Field field, Object value) {
        if (value == null) {
            setField(object, field, Optional.empty());
        } else {
            Type genericType = field.getGenericType();
            if (!(genericType instanceof ParameterizedTypeImpl)) {
                throw exception("Raw types are not supported. Field '%s' is 'Optional'", field.getName());
            }
            Type parameterType = ((ParameterizedTypeImpl) genericType).getActualTypeArguments()[0];
            if (!(parameterType instanceof Class<?>)) {
                throw exception("Wildcards are not supported. Field '%s' is 'Optional<%s>'", field.getName(), parameterType);
            }

            if (value.getClass() != parameterType) {
                throw exception("Cannot assign value of type 'Optional<%s>' to field '%s' of type 'Optional<%s>'", value.getClass().getName(), field.getName(), parameterType.getTypeName());
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

    private static Stream<Field> fieldsOf(Class<?> targetClass) {
        Stream<Field> fields = Stream.empty();
        while (targetClass != Object.class) {
            fields = Stream.concat(fields, Arrays.stream(targetClass.getDeclaredFields()));
            targetClass = targetClass.getSuperclass();
        }
        return fields
                .filter(field -> (field.getModifiers() & Modifier.STATIC) == 0)
                .filter(field -> !field.isSynthetic());
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
