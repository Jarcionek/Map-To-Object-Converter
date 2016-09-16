package uk.co.jpawlak.maptoobjectconverter;

import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterException;
import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterUnknownException;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utility class that allows to easily convert Map&lt;String, Object&gt; into staticly typed object.
 * <br><br>
 * Values are mapped to fields using maps' keys and fields' names. Fields can be private final, no methods or annotations are required.
 * The instance will be created without calling a constructor. All non-static fields in created objects are guaranteed
 * to be non-null.
 * <br><br>
 * See example usages in readme file available in <a href="https://github.com/Jarcionek/Map-To-Object-Converter">project GitHub repository</a>.
 *
 * @see #convert(Map, Class)
 */
public class MapToObjectConverter {

    private final boolean keyCaseSensitive;
    private final Converters converters;
    private final Checker checker;
    private final ObjectCreator objectCreator;

    public MapToObjectConverter() {
        this(true);
    }

    public MapToObjectConverter(boolean keyCaseSensitive) {
        this.keyCaseSensitive = keyCaseSensitive;
        this.converters = new Converters();
        this.checker = new Checker(converters, keyCaseSensitive);
        this.objectCreator = new ObjectCreator(converters);
    }

    /**
     * Converts Map&lt;String, Object&gt; into an instance of <code>targetClass</code>.
     *
     * @param map map to convert into object
     * @param targetClass a class whose instance will be created
     * @param <T> the type of <code>targetClass</code>
     * @return an instance of <code>targetClass</code>
     * @throws uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterException or any of its subclasses
     */
    public <T> T convert(Map<String, Object> map, Class<T> targetClass) throws ConverterException {
        try {
            checker.checkParameters(map, targetClass);
            if (!keyCaseSensitive) {
                Map<String, Object> newMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                newMap.putAll(map);
                map = newMap;
            }
            checker.checkKeysEqualToFieldsNames(map.keySet(), targetClass);
            checker.checkOptionalFieldsForNullValues(map, targetClass);

            return objectCreator.convertMapToObject(map, targetClass);
        } catch (Exception e) {
            throw e instanceof ConverterException ? (ConverterException) e : new ConverterUnknownException(e);
        }
    }

    public <T> MapToObjectConverter registerConverter(Class<T> aClass, SingleValueConverter<T> singleValueConverter) {
        converters.registerConverter(aClass, singleValueConverter);
        return this;
    }

    public MapToObjectConverter registerConverter(Type type, SingleValueConverter<?> singleValueConverter) {
        converters.registerConverter(type, singleValueConverter);
        return this;
    }

}
