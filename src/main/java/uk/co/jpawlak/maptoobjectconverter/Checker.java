package uk.co.jpawlak.maptoobjectconverter;

import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterIllegalArgumentException;
import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterMissingFieldsException;
import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterMissingValuesException;
import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterNullValueException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static uk.co.jpawlak.maptoobjectconverter.Utils.fieldsOf;

class Checker {

    private final Converters converters;
    private final boolean keyCaseSensitive;

    Checker(Converters converters, boolean keyCaseSensitive) {
        this.converters = converters;
        this.keyCaseSensitive = keyCaseSensitive;
    }

    void checkParameters(Map<String, ?> map, Class<?> targetClass) {
        if (map == null) {
            throw new ConverterIllegalArgumentException("Map cannot be null.");
        }
        try {
            if (map.containsKey(null)) {
                throw new ConverterIllegalArgumentException("Map's keys cannot be null.");
            }
        } catch (NullPointerException ignored) {
            // map does not permit null keys
        }
        if (targetClass == null) {
            throw new ConverterIllegalArgumentException("Target class cannot be null.");
        }
        if (targetClass.isPrimitive()) {
            throw new ConverterIllegalArgumentException("Cannot convert map to primitive type.");
        }
        if (targetClass.isEnum()) {
            throw new ConverterIllegalArgumentException("Cannot convert map to enum.");
        }
        if (targetClass.isAnnotation()) {
            throw new ConverterIllegalArgumentException("Cannot convert map to annotation.");
        }
        if (targetClass.isInterface()) {
            throw new ConverterIllegalArgumentException("Cannot convert map to interface.");
        }
        if ((targetClass.getModifiers() & Modifier.ABSTRACT) != 0) {
            throw new ConverterIllegalArgumentException("Cannot convert map to abstract class.");
        }
        if (!keyCaseSensitive) {
            List<String> keysDuplicates = map.keySet().stream()
                    .filter(key1 -> map.keySet().stream()
                            .filter(key2 -> key1.equalsIgnoreCase(key2) && !key1.equals(key2))
                            .findFirst()
                            .isPresent()
                    )
                    .collect(toList());
            if (!keysDuplicates.isEmpty()) {
                throw new ConverterIllegalArgumentException("Keys '%s' are duplicates (converter is key case insensitive).", keysDuplicates.stream().collect(joining("', '")));
            }

        }
    }

    void checkKeysEqualToFieldsNames(Set<String> keys, Class<?> targetClass) {
        Set<String> fieldsNames = fieldsOf(targetClass)
                .map(Field::getName)
                .collect(toCollection(LinkedHashSet::new));

        checkKeysEqualToFieldsNames(keys, fieldsNames);
    }

    private void checkKeysEqualToFieldsNames(Set<String> keys, Set<String> fieldsNames) {
        Set<String> missingFields = keys.stream().filter(key -> !contains(fieldsNames, key)).collect(toCollection(LinkedHashSet::new));
        if (!missingFields.isEmpty()) {
            throw new ConverterMissingFieldsException("No fields for keys: '%s'.", missingFields.stream().collect(joining("', '")));
        }

        Set<String> missingValues = fieldsNames.stream().filter(fieldName -> !contains(keys, fieldName)).collect(toCollection(LinkedHashSet::new));
        if (!missingValues.isEmpty()) {
            throw new ConverterMissingValuesException("No values for fields: '%s'.", missingValues.stream().collect(joining("', '")));
        }
    }

    private boolean contains(Set<String> set, String string) {
        if (keyCaseSensitive) {
            return set.contains(string);
        } else {
            return set.stream()
                    .filter(value -> value.equalsIgnoreCase(string))
                    .findFirst()
                    .isPresent();
        }
    }

    void checkOptionalFieldsForNullValues(Map<String, Object> map, Class<?> targetClass) {
        Set<String> fieldsNames = fieldsOf(targetClass)
                .filter(field -> field.getType() != Optional.class && map.get(field.getName()) == null)
                .filter(field -> !converters.hasRegisteredConverterFor(field.getGenericType()))
                .map(Field::getName)
                .collect(toCollection(LinkedHashSet::new));

        if (!fieldsNames.isEmpty()) {
            throw new ConverterNullValueException("Null values require fields to be Optional. Null values for fields: '%s'.", fieldsNames.stream().collect(joining("', '")));
        }
    }

}
