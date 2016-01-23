package uk.co.jpawlak.maptoobjectconverter;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class MapToObjectConverter {

    public <T> T convert(Map<String, Object> map, Class<T> aClass) {
        try {
            T result = aClass.getConstructor().newInstance();

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                aClass.getDeclaredField(entry.getKey()).set(result, entry.getValue());
            }

            return result;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

}
