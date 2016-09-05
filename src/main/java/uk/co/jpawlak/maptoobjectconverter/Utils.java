package uk.co.jpawlak.maptoobjectconverter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

class Utils {

    static Stream<Field> fieldsOf(Class<?> targetClass) {
        Stream<Field> fields = Stream.empty();
        while (targetClass != Object.class) {
            fields = Stream.concat(fields, stream(targetClass.getDeclaredFields()));
            targetClass = targetClass.getSuperclass();
        }
        return fields
                .filter(field -> (field.getModifiers() & Modifier.STATIC) == 0)
                .filter(field -> !field.isSynthetic());
    }

}
