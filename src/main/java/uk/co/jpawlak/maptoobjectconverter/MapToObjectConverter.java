package uk.co.jpawlak.maptoobjectconverter;

import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

public class MapToObjectConverter {

    private static final ReflectionFactory REFLECTION_FACTORY = ReflectionFactory.getReflectionFactory();

    public <T> T convert(Map<String, Object> map, Class<T> aClass) {
        try {
            if (isBasicType(aClass)) {
                if (map.size() != 1) {
                    throw new IllegalArgumentException(String.format("Cannot map non-singleton map to single basic type '%s'. Keys found: '%s'", String.class.getSimpleName(), map.keySet().stream().collect(joining("', '"))));
                } else {
                    T t = (T) map.values().stream().findFirst().get();
                    if (aClass.isInstance(t) || aClass.isPrimitive()) {
                        return t;
                    } else {
                        throw new IllegalArgumentException(String.format("Cannot convert type '%s' to basic type '%s'", t.getClass().getSimpleName(), aClass.getSimpleName()));
                    }
                }
            }

            T result = createInstance(aClass);

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Field field = aClass.getDeclaredField(entry.getKey());
                field.setAccessible(true);
                if (entry.getValue() == null) {
                    if (field.getType() == Optional.class) {
                        field.set(result, Optional.empty());
                    } else {
                        throw new IllegalArgumentException(String.format("field '%s' was null, make it Optional", field.getName()));
                    }
                } else {
                    if (field.getType() == Optional.class) {
                        field.set(result, Optional.of(entry.getValue()));
                    } else {
                        field.set(result, entry.getValue());
                    }
                }
            }

            return result;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isBasicType(Class<?> aClass) {
        return aClass.isPrimitive()
                || aClass == String.class
                || aClass == Character.class
                || aClass == Boolean.class
                || aClass == Byte.class
                || aClass == Short.class
                || aClass == Integer.class
                || aClass == Long.class
                || aClass == Float.class
                || aClass == Double.class;
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

}
