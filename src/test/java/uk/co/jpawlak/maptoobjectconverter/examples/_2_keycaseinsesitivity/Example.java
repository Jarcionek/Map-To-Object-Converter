package uk.co.jpawlak.maptoobjectconverter.examples._2_keycaseinsesitivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.co.jpawlak.maptoobjectconverter.MapToObjectConverter;
import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterIllegalArgumentException;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;

public class Example {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void convertsToObjectInCaseSensitiveMode() {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("abc", 1);
        dataMap.put("aBC", 2);

        MapToObjectConverter converter = new MapToObjectConverter(true); // keyCaseSensitive = true (default)

        Data data = converter.convert(dataMap, Data.class);

        assertEquals(1, data.abc);
        assertEquals(2, data.aBC);
        // multiple assertions give poor diagnostics, use shazamcrest instead
    }

    @Test
    public void throwsExceptionForDuplicateKeysInCaseInsensitiveMode() {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("abc", 1);
        dataMap.put("aBC", 2);

        MapToObjectConverter converter = new MapToObjectConverter(false); // keyCaseSensitive = false

        expectedException.expect(ConverterIllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Keys 'abc', 'aBC' are duplicates (converter is key case insensitive)."));

        converter.convert(dataMap, Data.class);
    }

    @Test
    public void convertsToObjectInCaseInsensitiveMode() {
        Map<String, Object> dataMap = singletonMap("ABC", 7);

        MapToObjectConverter converter = new MapToObjectConverter(false); // keyCaseSensitive = false

        Data data = converter.convert(dataMap, Data.class);

        assertEquals(7, data.abc);
        assertEquals(7, data.aBC);
        // multiple assertions give poor diagnostics, use shazamcrest instead
    }

}
