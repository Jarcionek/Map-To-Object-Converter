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
import static org.hamcrest.CoreMatchers.instanceOf;

public class MapToObjectConverterTest_SingleValueConverter {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final MapToObjectConverter mapToObjectConverter = new MapToObjectConverter();



    @Test
    public void throwsExceptionWhenTryingToRegisterConverterForJavaOptional() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot register convert for 'java.util.Optional'. Register converter for the type parameter instead."));

        mapToObjectConverter.registerConverter(Optional.class, value -> null);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

    @Test
    public void throwsExceptionWhenRegisteredConverterThrowsException() {
        Map<String, Object> map = ImmutableMap.of(
                "string", "whatever",
                "number", 2
        );

        mapToObjectConverter
                .registerConverter(String.class, value -> "x")
                .registerConverter(int.class, value -> {
                    throw new NullPointerException();
                });

        expectedException.expect(RegisteredConverterException.class);
        expectedException.expectCause(instanceOf(NullPointerException.class));

        mapToObjectConverter.convert(map, SimpleClass.class);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

    @Test
    public void throwsExceptionWhenRegisteredConverterThrowsExceptionForOptionalField() {
        Map<String, Object> map = singletonMap("optionalNumber", 35);

        mapToObjectConverter
                .registerConverter(Integer.class, value -> {
                    throw new NullPointerException();
                });

        expectedException.expect(RegisteredConverterException.class);
        expectedException.expectCause(instanceOf(NullPointerException.class));

        mapToObjectConverter.convert(map, ClassWithOptionalField.class);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private enum Enum {
        VALUE_OF, CONVERTER
    }

    private static class ClassWithEnumFields {
        Enum enumField;
        Optional<Enum> optionalEnumField;
    }

    @Test
    public void usesRegisteredEnumConverterInsteadOfValueOf() {
        Map<String, Object> map = ImmutableMap.of(
                "enumField", "VALUE_OF",
                "optionalEnumField", "VALUE_OF"
        );

        mapToObjectConverter
                .registerConverter(Enum.class, value -> Enum.CONVERTER);

        ClassWithEnumFields actual = mapToObjectConverter.convert(map, ClassWithEnumFields.class);

        ClassWithEnumFields expected = new ClassWithEnumFields();
        expected.enumField = Enum.CONVERTER;
        expected.optionalEnumField = Optional.of(Enum.CONVERTER);

        assertThat(actual, sameBeanAs(expected));
    }

}
