package uk.co.jpawlak.maptoobjectconverter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;
import java.util.Optional;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static java.util.Collections.singletonMap;

public class MapToObjectConverterTest_ConvertingToClassesWithOptionalFields {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final MapToObjectConverter mapToObjectConverter = new MapToObjectConverter();



    private static class ClassWithOptionalField {
        Optional<String> optionalAddress;
    }

    @Test
    public void setsOptionalEmptyWhenValueIsNull() {
        Map<String, Object> map = singletonMap("optionalAddress", null);

        ClassWithOptionalField actual = mapToObjectConverter.convert(map, ClassWithOptionalField.class);

        ClassWithOptionalField expected = new ClassWithOptionalField();
        expected.optionalAddress = Optional.empty();

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void setsOptionalOfTheValueWhenValueIsNotNull() {
        Map<String, Object> map = singletonMap("optionalAddress", "123");

        ClassWithOptionalField actual = mapToObjectConverter.convert(map, ClassWithOptionalField.class);

        ClassWithOptionalField expected = new ClassWithOptionalField();
        expected.optionalAddress = Optional.of("123");

        assertThat(actual, sameBeanAs(expected));
    }

}
