package uk.co.jpawlak.maptoobjectconverter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;

@SuppressWarnings("unused")
public class MapToObjectConverterTest_OptionalFields {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final MapToObjectConverter mapToObjectConverter = new MapToObjectConverter();



    private static class ClassWithNonOptionalFields {
        String street;
        String postcode;
    }

    @Test
    public void throwsExceptionWhenSingleValueIsNull() {
        Map<String, Object> map = new HashMap<>();
        map.put("street", null);
        map.put("postcode", "ABC 123");

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Null values require fields to be Optional. Null values for fields: 'street'");

        mapToObjectConverter.convert(map, ClassWithNonOptionalFields.class);
    }

    @Test
    public void throwsExceptionWhenMultipleValuesAreNull() {
        Map<String, Object> map = new HashMap<>();
        map.put("street", null);
        map.put("postcode", null);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Null values require fields to be Optional. Null values for fields: 'street', 'postcode'");

        mapToObjectConverter.convert(map, ClassWithNonOptionalFields.class);
    }



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
        expectedException.expectMessage(equalTo("Cannot assign value of type 'Optional<java.lang.Integer>' to field 'optionalAddress' of type 'Optional<java.lang.String>'"));

        mapToObjectConverter.convert(map, ClassWithOptionalField.class);
    }



    private static class ClassWithOptionalWildcardField {
        Optional<?> x;
    }

    @Test
    public void throwsExceptionForClassWithOptionalWildcardField() {
        Map<String, Object> map = singletonMap("x", "abc");

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Wildcards are not supported. Field 'x' is 'Optional<?>'"));

        mapToObjectConverter.convert(map, ClassWithOptionalWildcardField.class);
    }



    private static class ClassWithOptionalBoundedWildcardField<T extends Number> {
        Optional<T> x;
    }

    @Test
    public void throwsExceptionForClassWithOptionalBoundedWildcardField() {
        Map<String, Object> map = singletonMap("x", "abc");

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Wildcards are not supported. Field 'x' is 'Optional<T>'"));

        mapToObjectConverter.convert(map, ClassWithOptionalBoundedWildcardField.class);
    }



    private static class ClassWithRawOptional {
        Optional z;
    }

    @Test
    public void throwsExceptionForClassWithRawOptional() {
        Map<String, Object> map = singletonMap("z", "abc");

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Raw types are not supported. Field 'z' is 'Optional'");

        mapToObjectConverter.convert(map, ClassWithRawOptional.class);
    }

}
