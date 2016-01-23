package uk.co.jpawlak.maptoobjectconverter;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Map;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;

public class MapToObjectConverterTest {

    private final MapToObjectConverter mapToObjectConverter = new MapToObjectConverter();



    public static class SimpleClass {
        String propertyName;
    }

    @Test
    public void convertsMapToObjectOfSpecifiedClass() {
        Map<String, Object> map = ImmutableMap.of(
                "propertyName", "stringValue"
        );

        SimpleClass actual = mapToObjectConverter.convert(map, SimpleClass.class);

        SimpleClass expected = new SimpleClass();
        expected.propertyName = "stringValue";

        assertThat(actual, sameBeanAs(expected));
    }

}
