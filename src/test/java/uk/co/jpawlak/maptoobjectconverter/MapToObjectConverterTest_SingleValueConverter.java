package uk.co.jpawlak.maptoobjectconverter;

import com.google.common.collect.ImmutableMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;

@SuppressWarnings({"unused", "unchecked"})
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

    @Test
    public void throwsExceptionWhenTryingToRegisterNullConverter() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Registered converter cannot be null."));

        mapToObjectConverter.registerConverter(String.class, null);
    }

    @Test
    public void throwsExceptionWhenTryingToRegisterConverterForNullType() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot register converter for null class."));

        mapToObjectConverter.registerConverter(null, v -> v);
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
        expectedException.expectMessage(equalTo("Null values require fields to be Optional. Registered converter for type 'java.lang.String' returned null."));

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

    @Test
    public void usesRegisteredConverterEvenWhenOriginalValueIsNull() {
        Map<String, Object> map = new HashMap<>();
        map.put("string", null);
        map.put("number", 0);

        mapToObjectConverter
                .registerConverter(String.class, value -> "non null");

        SimpleClass actual = mapToObjectConverter.convert(map, SimpleClass.class);

        SimpleClass expected = new SimpleClass();
        expected.string = "non null";
        expected.number = 0;

        assertThat(actual, sameBeanAs(expected));
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
    public void setsOptionalEmptyWhenRegisteredConverterReturnsNull() {
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

    @Test
    public void throwsExceptionWhenRegisteredConverterReturnsValueOfDifferentTypeThanTypeParameter() {
        Map<String, Object> map = singletonMap("optionalNumber", 1234);

        mapToObjectConverter.registerConverter(Integer.class, (SingleValueConverter) value -> "a string");

        expectedException.expect(RegisteredConverterException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'Optional<java.lang.String>' returned by registered converter to field 'optionalNumber' of type 'Optional<java.lang.Integer>'"));

        mapToObjectConverter.convert(map, ClassWithOptionalField.class);
    }

    @Test
    public void usesValueReturnedByConverterWhenOriginalValueWasNull() {
        Map<String, Object> map = singletonMap("optionalNumber", null);

        mapToObjectConverter.registerConverter(Integer.class, value -> 852);

        ClassWithOptionalField actual = mapToObjectConverter.convert(map, ClassWithOptionalField.class);

        ClassWithOptionalField expected = new ClassWithOptionalField();
        expected.optionalNumber = Optional.of(852);

        assertThat(actual, sameBeanAs(expected));
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private class ClassWithPrimitiveArray {
        int[] numbers;
    }
    private class ClassWithOptionalPrimitiveArray {
        Optional<int[]> numbers;
    }

    private class ClassWithIntegerArray {
        Integer[] numbers;
    }

    @Test
    public void usesRegisteredConverterForPrimitiveArrayField() {
        Map<String, Object> map = singletonMap("numbers", "1,2,3");

        mapToObjectConverter.registerConverter(int[].class, value -> stream(value.toString().split(",")).mapToInt(Integer::parseInt).toArray());

        ClassWithPrimitiveArray actual = mapToObjectConverter.convert(map, ClassWithPrimitiveArray.class);

        ClassWithPrimitiveArray expected = new ClassWithPrimitiveArray();
        expected.numbers = new int[] {1, 2, 3};

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void usesRegisteredConverterForOptionalPrimitiveArrayField() {
        Map<String, Object> map = singletonMap("numbers", "2,4,8,16");

        mapToObjectConverter.registerConverter(int[].class, value -> stream(value.toString().split(",")).mapToInt(Integer::parseInt).toArray());

        ClassWithOptionalPrimitiveArray actual = mapToObjectConverter.convert(map, ClassWithOptionalPrimitiveArray.class);

        ClassWithOptionalPrimitiveArray expected = new ClassWithOptionalPrimitiveArray();
        expected.numbers = Optional.of(new int[] {2, 4, 8, 16});

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void usesRegisteredConverterForObjectArrayField() {
        Map<String, Object> map = singletonMap("numbers", "whatever");

        mapToObjectConverter.registerConverter(Integer[].class, value -> new Integer[] {7, 15});

        ClassWithIntegerArray actual = mapToObjectConverter.convert(map, ClassWithIntegerArray.class);

        ClassWithIntegerArray expected = new ClassWithIntegerArray();
        expected.numbers =  new Integer[] {7, 15};

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void throwsExceptionWithCorrectlyFormattedPrimitiveArrayTypeForTypeMismatch() {
        Map<String, Object> map = singletonMap("numbers", "1,2,3");

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'java.lang.String' to field 'numbers' of type 'int[]'"));

        mapToObjectConverter.convert(map, ClassWithPrimitiveArray.class);
    }

    @Test
    public void throwsExceptionWithCorrectlyFormattedObjectArrayTypeForTypeMismatch() {
        Map<String, Object> map = singletonMap("numbers", new int[] {1, 2, 3});

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'int[]' to field 'numbers' of type 'java.lang.Integer[]'"));

        mapToObjectConverter.convert(map, ClassWithIntegerArray.class);
    }

    @Test
    public void throwsExceptionWithCorrectlyFormattedObjectArrayTypeWhenConverterReturnsNullForNonOptionalField() {
        Map<String, Object> map = singletonMap("numbers", new int[] {1, 2, 3});

        mapToObjectConverter.registerConverter(Integer[].class, value -> null);

        expectedException.expect(RegisteredConverterException.class);
        expectedException.expectMessage(equalTo("Null values require fields to be Optional. Registered converter for type 'java.lang.Integer[]' returned null."));

        mapToObjectConverter.convert(map, ClassWithIntegerArray.class);
    }

    @Test
    public void throwsExceptionWithCorrectlyFormattedPrimitiveArrayTypeForOptionalTypeMismatch() {
        Map<String, Object> map = singletonMap("numbers", new Integer[] {1, 2, 3});

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'Optional<java.lang.Integer[]>' to field 'numbers' of type 'Optional<int[]>'"));

        mapToObjectConverter.convert(map, ClassWithOptionalPrimitiveArray.class);
    }

    @Test
    public void throwsExceptionWithCorrectlyFormattedPrimitiveArrayTypeForOptionalTypeMismatchWithRegisteredConverter() {
        Map<String, Object> map = singletonMap("numbers", "whatever");

        mapToObjectConverter.registerConverter(int[].class, (SingleValueConverter) value -> new Integer[][] {{5}});

        expectedException.expect(RegisteredConverterException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'Optional<java.lang.Integer[][]>' returned by registered converter to field 'numbers' of type 'Optional<int[]>'"));

        mapToObjectConverter.convert(map, ClassWithOptionalPrimitiveArray.class);
    }

}
