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

/**
 * @see #convert(Map, Class)
 */
public class MapToObjectConverter {

    private static final ReflectionFactory REFLECTION_FACTORY = ReflectionFactory.getReflectionFactory();

    private final Converters converters = new Converters();

    /**
     * Converts Map&lt;String, Object&gt; into an instance of <code>targetClass</code>.
     * <br><br>
     * Values are mapped to fields using keys and fields' names. Fields can be private final, no methods are required.
     * The instance will be created without calling a constructor. All non-static fields in created objects are guaranteed
     * to be non-null.
     * <br><br>
     * Example usage:
     *
     * <pre>
     * public enum Gender {
     *     MALE, FEMALE
     * }
     *
     * public class Employee {
     *
     *     public final String name;
     *     public final int age;
     *     public final Gender gender;
     *     public final Optional&lt;String&gt; phoneNumber;
     *
     *     public Employee(String name, int age, Gender gender, Optional&lt;String&gt; phoneNumber) {
     *         this.name = name;
     *         this.age = age;
     *         this.gender = gender;
     *         this.phoneNumber = phoneNumber;
     *     }
     *
     * }
     *
     * public class Example {
     *
     *     public static void main(String... args) {
     *         Map&lt;String, Object&gt; employeeMap = new HashMap&lt;&gt;();
     *         employeeMap.put("name", "Jaroslaw Pawlak");
     *         employeeMap.put("age", 26);
     *         employeeMap.put("gender", "MALE");
     *         employeeMap.put("phoneNumber", null);
     *
     *         MapToObjectConverter converter = new MapToObjectConverter();
     *
     *         Employee employee = converter.convert(employeeMap, Employee.class);
     *     }
     * }
     * </pre>
     *
     * @param map map to convert into object
     * @param targetClass a class whose instance will be created
     * @param <T> the type of <code>targetClass</code>
     * @return as instance of <code>targetClass</code>
     * @throws IllegalArgumentException if converting fails due to invalid usage or a mismatch between number/name of properties in the map and target class
     * @throws RuntimeException if converting fails for any other reason which was not considered before - if you get this, please report at https://github.com/Jarcionek/Map-To-Object-Converter
     */
    public <T> T convert(Map<String, Object> map, Class<T> targetClass) {
        checkParameters(map, targetClass);
        checkKeysEqualToFieldsNames(map.keySet(), targetClass);
        checkOptionalFieldsForNullValues(map, targetClass);

        T result = createInstance(targetClass);

        setFields(map, targetClass, result);

        return result;
    }

    public <T> MapToObjectConverter registerConverter(Class<T> aClass, SingleValueConverter<T> singleValueConverter) {
        converters.registerConverter(aClass, singleValueConverter);
        return this;
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

    private void checkOptionalFieldsForNullValues(Map<String, Object> map, Class<?> targetClass) {
        Set<String> fieldsNames = fieldsOf(targetClass)
                .filter(field -> field .getType() != Optional.class && map.get(field.getName()) == null)
                .filter(field -> !converters.hasConverterFor(field.getType()))
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

    private <T> void setFields(Map<String, Object> map, Class<T> targetClass, T result) {
        fieldsOf(targetClass).forEach(field -> {
            if (field.getType() == Optional.class) {
                setOptionalField(result, field, map.get(field.getName()));
            } else {
                SingleValueConverter<?> converter = converters.getConverterFor(field.getType());
                Object value = map.get(field.getName());
                Object convertedValue = converter.convert(value);
                if (convertedValue == null) {
                    throw new RegisteredConverterException(String.format("Null values require fields to be Optional. Registered converter for type '%s' returned null.", field.getType().getName()));
                }
                setField(result, field, convertedValue);
            }
        });
    }

    private void setOptionalField(Object object, Field field, Object value) {
        Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedTypeImpl)) {
            throw exception("Raw types are not supported. Field '%s' is 'Optional'", field.getName());
        }
        Type parameterType = ((ParameterizedTypeImpl) genericType).getActualTypeArguments()[0];
        if (!(parameterType instanceof Class<?>)) {
            throw exception("Wildcards are not supported. Field '%s' is 'Optional<%s>'", field.getName(), parameterType);
        }

        if (converters.hasConverterFor((Class<?>) parameterType)) {
            SingleValueConverter<?> converter = converters.getConverterFor((Class<?>) parameterType);
            Object convertedValue = converter.convert(value);
            if (convertedValue != null && convertedValue.getClass() != parameterType) {
                throw new RegisteredConverterException(String.format("Cannot assign value of type 'Optional<%s>' returned by registered converter to field '%s' of type 'Optional<%s>'", convertedValue.getClass().getName(), field.getName(), parameterType.getTypeName()));
            }
            setField(object, field, Optional.ofNullable(convertedValue));
        } else if (value == null) {
            setField(object, field, Optional.empty());
        } else {
            if (((Class<?>) parameterType).isEnum()) {
                setField(object, field, Optional.of(asEnum((Class<?>) parameterType, value)));
                return;
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
