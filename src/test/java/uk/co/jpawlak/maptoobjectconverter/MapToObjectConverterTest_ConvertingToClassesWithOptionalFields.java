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

    @Test
    public void throwsExceptionForTypeMismatch() {
        Map<String, Object> map = singletonMap("optionalAddress", 123);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Cannot assign value of type 'Optional<java.lang.Integer>' to field 'optionalAddress' of type 'Optional<java.lang.String>'");

        mapToObjectConverter.convert(map, ClassWithOptionalField.class);
    }



    private static class ClassWithOptionalWildcardField {
        Optional<?> x;
    }

    @Test
    public void throwsExceptionForClassWithOptionalWildcardField() {
        Map<String, Object> map = singletonMap("x", "abc");

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Wildcards are not supported. Field 'x' is 'Optional<?>'");

        mapToObjectConverter.convert(map, ClassWithOptionalWildcardField.class);
    }



    private static class ClassWithOptionalBoundedWildcardField<T extends Number> {
        Optional<T> x;
    }

    @Test
    public void throwsExceptionForClassWithOptionalBoundedWildcardField() {
        Map<String, Object> map = singletonMap("x", "abc");

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Wildcards are not supported. Field 'x' is 'Optional<T>'");

        mapToObjectConverter.convert(map, ClassWithOptionalBoundedWildcardField.class);
    }

}
