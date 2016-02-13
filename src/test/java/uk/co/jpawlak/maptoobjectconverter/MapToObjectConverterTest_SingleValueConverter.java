package uk.co.jpawlak.maptoobjectconverter;

import com.google.common.collect.ImmutableMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;
import java.util.Optional;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;

public class MapToObjectConverterTest_SingleValueConverter {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final MapToObjectConverter mapToObjectConverter = new MapToObjectConverter();



    public static class SimpleClass {
        String string;
        int number;
    }

    @Test
    public void usesRegisteredSingleValueConverter() {
        Map<String, Object> map = ImmutableMap.of(
                "string", "original value",
                "number", 3
        );

        mapToObjectConverter.registerConverter(String.class, value -> "converted value");

        SimpleClass actual = mapToObjectConverter.convert(map, SimpleClass.class);

        SimpleClass expected = new SimpleClass();
        expected.string = "converted value";
        expected.number = 3;

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void usesMultipleRegisteredSingleValueConverters() {
        Map<String, Object> map = ImmutableMap.of(
                "string", "original value 2",
                "number", "4"
        );

        mapToObjectConverter
                .registerConverter(String.class, value -> "converted value 2")
                .registerConverter(int.class, value -> Integer.parseInt(value.toString()));

        SimpleClass actual = mapToObjectConverter.convert(map, SimpleClass.class);

        SimpleClass expected = new SimpleClass();
        expected.string = "converted value 2";
        expected.number = 4;

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void throwsExceptionWhenConverterReturnsNullAndFieldIsNotOptional() {
        Map<String, Object> map = ImmutableMap.of(
                "string", "whatever",
                "number", 2
        );

        mapToObjectConverter
                .registerConverter(String.class, value -> null)
                .registerConverter(int.class, value -> (int) value);

        expectedException.expect(RegisteredConverterException.class);
        expectedException.expectMessage(equalTo("Null values require fields to be Optional. Registered Converter for type 'String' returned null for field 'string'"));

        mapToObjectConverter.convert(map, SimpleClass.class);
    }



    public static class ClassWithOptionalField {
        Optional<Integer> optionalNumber;
    }

    @Test
    public void usesRegisteredConverterForOptionalField() {
        Map<String, Object> map = singletonMap("optionalNumber", 15);

        mapToObjectConverter.registerConverter(Integer.class, value -> (int) value * 2);

        ClassWithOptionalField actual = mapToObjectConverter.convert(map, ClassWithOptionalField.class);

        ClassWithOptionalField expected = new ClassWithOptionalField();
        expected.optionalNumber = Optional.of(30);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void setsOptionalEmptyWhenRegistedConverterReturnsNull() {
        Map<String, Object> map = singletonMap("optionalNumber", 35);

        mapToObjectConverter.registerConverter(Integer.class, value -> null);

        ClassWithOptionalField actual = mapToObjectConverter.convert(map, ClassWithOptionalField.class);

        ClassWithOptionalField expected = new ClassWithOptionalField();
        expected.optionalNumber = Optional.empty();

        assertThat(actual, sameBeanAs(expected));
    }

}
