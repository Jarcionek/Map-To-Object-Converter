package uk.co.jpawlak.maptoobjectconverter;

import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;

public class MapToObjectConverter {

    private static final ReflectionFactory REFLECTION_FACTORY = ReflectionFactory.getReflectionFactory();

    public <T> T convert(Map<String, Object> map, Class<T> aClass) {
        try {
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
