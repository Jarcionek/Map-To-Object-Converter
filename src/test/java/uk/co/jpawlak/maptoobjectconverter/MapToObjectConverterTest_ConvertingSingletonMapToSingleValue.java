package uk.co.jpawlak.maptoobjectconverter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;

public class MapToObjectConverterTest_ConvertingSingletonMapToSingleValue {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final MapToObjectConverter mapToObjectConverter = new MapToObjectConverter();

    // mapping to unboxed primitives ///////////////////////////////////////////////////////////////////////////////////

    @Test
    public void convertsSingletonMapToUnboxedChar() {
        Map<String, Object> map = singletonMap("randomTextIgnoredByConverter_1", 'c');

        char actual = mapToObjectConverter.convert(map, char.class);

        assertThat(actual, equalTo('c'));
    }

    @Test
    public void convertsSingletonMapToUnboxedBoolean() {
        Map<String, Object> map = singletonMap("randomTextIgnoredByConverter_2", true);

        boolean actual = mapToObjectConverter.convert(map, boolean.class);

        assertThat(actual, equalTo(true));
    }

    @Test
    public void convertsSingletonMapToUnboxedByte() {
        Map<String, Object> map = singletonMap("randomTextIgnoredByConverter_3", (byte) 5);

        byte actual = mapToObjectConverter.convert(map, byte.class);

        assertThat(actual, equalTo((byte) 5));
    }

    @Test
    public void convertsSingletonMapToUnboxedShort() {
        Map<String, Object> map = singletonMap("randomTextIgnoredByConverter_4", (short) 10);

        short actual = mapToObjectConverter.convert(map, short.class);

        assertThat(actual, equalTo((short) 10));
    }

    @Test
    public void convertsSingletonMapToUnboxedInt() {
        Map<String, Object> map = singletonMap("randomTextIgnoredByConverter_5", 15);

        int actual = mapToObjectConverter.convert(map, int.class);

        assertThat(actual, equalTo(15));
    }

    @Test
    public void convertsSingletonMapToUnboxedLong() {
        Map<String, Object> map = singletonMap("randomTextIgnoredByConverter_6", 20L);

        long actual = mapToObjectConverter.convert(map, long.class);

        assertThat(actual, equalTo(20L));
    }

    @Test
    public void convertsSingletonMapToUnboxedFloat() {
        Map<String, Object> map = singletonMap("randomTextIgnoredByConverter_7", 0.125f);

        float actual = mapToObjectConverter.convert(map, float.class);

        assertThat(actual, equalTo(0.125f));
    }

    @Test
    public void convertsSingletonMapToUnboxedDouble() {
        Map<String, Object> map = singletonMap("randomTextIgnoredByConverter_8", 0.0625d);

        double actual = mapToObjectConverter.convert(map, double.class);

        assertThat(actual, equalTo(0.0625d));
    }

    // mapping to boxed primitives /////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void convertsSingletonMapToBoxedChar() {
        Map<String, Object> map = singletonMap("randomTextIgnoredByConverter_9", 'c');

        Character actual = mapToObjectConverter.convert(map, Character.class);

        assertThat(actual, equalTo('c'));
    }

    @Test
    public void convertsSingletonMapToBoxedBoolean() {
        Map<String, Object> map = singletonMap("randomTextIgnoredByConverter_10", true);

        Boolean actual = mapToObjectConverter.convert(map, Boolean.class);

        assertThat(actual, equalTo(true));
    }

    @Test
    public void convertsSingletonMapToBoxedByte() {
        Map<String, Object> map = singletonMap("randomTextIgnoredByConverter_11", (byte) 5);

        Byte actual = mapToObjectConverter.convert(map, Byte.class);

        assertThat(actual, equalTo((byte) 5));
    }

    @Test
    public void convertsSingletonMapToBoxedShort() {
        Map<String, Object> map = singletonMap("randomTextIgnoredByConverter_12", (short) 10);

        Short actual = mapToObjectConverter.convert(map, Short.class);

        assertThat(actual, equalTo((short) 10));
    }

    @Test
    public void convertsSingletonMapToBoxedInt() {
        Map<String, Object> map = singletonMap("randomTextIgnoredByConverter_13", 15);

        Integer actual = mapToObjectConverter.convert(map, Integer.class);

        assertThat(actual, equalTo(15));
    }

    @Test
    public void convertsSingletonMapToBoxedLong() {
        Map<String, Object> map = singletonMap("randomTextIgnoredByConverter_14", 20L);

        Long actual = mapToObjectConverter.convert(map, Long.class);

        assertThat(actual, equalTo(20L));
    }

    @Test
    public void convertsSingletonMapToBoxedFloat() {
        Map<String, Object> map = singletonMap("randomTextIgnoredByConverter_15", 0.125f);

        Float actual = mapToObjectConverter.convert(map, Float.class);

        assertThat(actual, equalTo(0.125f));
    }

    @Test
    public void convertsSingletonMapToBoxedDouble() {
        Map<String, Object> map = singletonMap("randomTextIgnoredByConverter_16", 0.0625d);

        Double actual = mapToObjectConverter.convert(map, Double.class);

        assertThat(actual, equalTo(0.0625d));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void convertsSingletonMapToString() {
        Map<String, Object> map = singletonMap("randomTextIgnoredByConverter_17", "hello");

        String actual = mapToObjectConverter.convert(map, String.class);

        assertThat(actual, equalTo("hello"));
    }
    
    

    private static class ArbitraryType {
        private final String a = "a";
        private final int one = 1;
    }

    @Test
    public void convertsSingletonMapWithValueOfArbitraryTypeToSingleValueOfThisType() {
        Map<String, Object> map = singletonMap("randomTextIgnoredByConverter", new ArbitraryType());

        ArbitraryType actual = mapToObjectConverter.convert(map, ArbitraryType.class);

        assertThat(actual, sameBeanAs(new ArbitraryType()));
    }

}
