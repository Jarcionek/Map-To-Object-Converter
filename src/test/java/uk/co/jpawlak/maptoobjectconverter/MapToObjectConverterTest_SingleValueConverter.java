package uk.co.jpawlak.maptoobjectconverter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterIllegalArgumentException;
import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterNullValueException;
import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterTypeMismatchException;
import uk.co.jpawlak.maptoobjectconverter.exceptions.RegisteredConverterException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static uk.co.jpawlak.maptoobjectconverter.TestUtil.assertObjectsEqual;

@SuppressWarnings({"unused", "unchecked"})
public class MapToObjectConverterTest_SingleValueConverter {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final MapToObjectConverter mapToObjectConverter = new MapToObjectConverter();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Illegal argument

    @Test
    public void throwsExceptionWhenTryingToRegisterConverterForJavaOptional() {
        expectedException.expect(ConverterIllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot register convert for 'java.util.Optional'. Register converter for the type parameter instead."));

        mapToObjectConverter.registerConverter(Optional.class, value -> null);
    }

    @Test
    public void throwsExceptionWhenTryingToRegisterNullConverter() {
        expectedException.expect(ConverterIllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Registered converter cannot be null."));

        mapToObjectConverter.registerConverter(String.class, null);
    }

    @Test
    public void throwsExceptionWhenTryingToRegisterConverterForNullType() {
        expectedException.expect(ConverterIllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot register converter for null class."));

        mapToObjectConverter.registerConverter(null, v -> v);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Non-optional fields

    public static class SimpleClass {
        String string;
        int number;
    }

    @Test
    public void usesRegisteredSingleValueConverter() {
        Map<String, Object> map = Map.of(
                "string", "original value",
                "number", 3
        );

        mapToObjectConverter.registerConverter(String.class, value -> "converted value");

        SimpleClass actual = mapToObjectConverter.convert(map, SimpleClass.class);

        SimpleClass expected = new SimpleClass();
        expected.string = "converted value";
        expected.number = 3;

        assertObjectsEqual(actual, expected);
    }

    @Test
    public void usesMultipleRegisteredSingleValueConverters() {
        Map<String, Object> map = Map.of(
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

        assertObjectsEqual(actual, expected);
    }

    @Test
    public void throwsExceptionWhenConverterReturnsNullAndFieldIsNotOptional() {
        Map<String, Object> map = Map.of(
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
        Map<String, Object> map = Map.of(
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

        assertObjectsEqual(actual, expected);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Optional fields

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

        assertObjectsEqual(actual, expected);
    }

    @Test
    public void setsOptionalEmptyWhenRegisteredConverterReturnsNull() {
        Map<String, Object> map = singletonMap("optionalNumber", 35);

        mapToObjectConverter.registerConverter(Integer.class, value -> null);

        ClassWithOptionalField actual = mapToObjectConverter.convert(map, ClassWithOptionalField.class);

        ClassWithOptionalField expected = new ClassWithOptionalField();
        expected.optionalNumber = Optional.empty();

        assertObjectsEqual(actual, expected);
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
        expectedException.expectMessage(equalTo("Cannot assign value of type 'Optional<java.lang.String>' returned by registered converter to field 'optionalNumber' of type 'Optional<java.lang.Integer>'."));

        mapToObjectConverter.convert(map, ClassWithOptionalField.class);
    }

    @Test
    public void usesValueReturnedByConverterWhenOriginalValueWasNull() {
        Map<String, Object> map = singletonMap("optionalNumber", null);

        mapToObjectConverter.registerConverter(Integer.class, value -> 852);

        ClassWithOptionalField actual = mapToObjectConverter.convert(map, ClassWithOptionalField.class);

        ClassWithOptionalField expected = new ClassWithOptionalField();
        expected.optionalNumber = Optional.of(852);

        assertObjectsEqual(actual, expected);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Non-optional and optional enum fields

    private enum Enum {
        VALUE_OF, CONVERTER
    }

    private static class ClassWithEnumFields {
        Enum enumField;
        Optional<Enum> optionalEnumField;
    }

    @Test
    public void usesRegisteredEnumConverterInsteadOfValueOf() {
        Map<String, Object> map = Map.of(
                "enumField", "VALUE_OF",
                "optionalEnumField", "VALUE_OF"
        );

        mapToObjectConverter
                .registerConverter(Enum.class, value -> Enum.CONVERTER);

        ClassWithEnumFields actual = mapToObjectConverter.convert(map, ClassWithEnumFields.class);

        ClassWithEnumFields expected = new ClassWithEnumFields();
        expected.enumField = Enum.CONVERTER;
        expected.optionalEnumField = Optional.of(Enum.CONVERTER);

        assertObjectsEqual(actual, expected);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Arrays fields

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

        assertObjectsEqual(actual, expected);
    }

    @Test
    public void usesRegisteredConverterForOptionalPrimitiveArrayField() {
        Map<String, Object> map = singletonMap("numbers", "2,4,8,16");

        mapToObjectConverter.registerConverter(int[].class, value -> stream(value.toString().split(",")).mapToInt(Integer::parseInt).toArray());

        ClassWithOptionalPrimitiveArray actual = mapToObjectConverter.convert(map, ClassWithOptionalPrimitiveArray.class);

        ClassWithOptionalPrimitiveArray expected = new ClassWithOptionalPrimitiveArray();
        expected.numbers = Optional.of(new int[] {2, 4, 8, 16});

        assertObjectsEqual(actual, expected);
    }

    @Test
    public void usesRegisteredConverterForObjectArrayField() {
        Map<String, Object> map = singletonMap("numbers", "whatever");

        mapToObjectConverter.registerConverter(Integer[].class, value -> new Integer[] {7, 15});

        ClassWithIntegerArray actual = mapToObjectConverter.convert(map, ClassWithIntegerArray.class);

        ClassWithIntegerArray expected = new ClassWithIntegerArray();
        expected.numbers =  new Integer[] {7, 15};

        assertObjectsEqual(actual, expected);
    }

    @Test
    public void throwsExceptionWithCorrectlyFormattedPrimitiveArrayTypeForTypeMismatch() {
        Map<String, Object> map = singletonMap("numbers", "1,2,3");

        expectedException.expect(ConverterTypeMismatchException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'java.lang.String' to field 'numbers' of type 'int[]'."));

        mapToObjectConverter.convert(map, ClassWithPrimitiveArray.class);
    }

    @Test
    public void throwsExceptionWithCorrectlyFormattedObjectArrayTypeForTypeMismatch() {
        Map<String, Object> map = singletonMap("numbers", new int[] {1, 2, 3});

        expectedException.expect(ConverterTypeMismatchException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'int[]' to field 'numbers' of type 'java.lang.Integer[]'."));

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

        expectedException.expect(ConverterTypeMismatchException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'Optional<java.lang.Integer[]>' to field 'numbers' of type 'Optional<int[]>'."));

        mapToObjectConverter.convert(map, ClassWithOptionalPrimitiveArray.class);
    }

    @Test
    public void throwsExceptionWithCorrectlyFormattedPrimitiveArrayTypeForOptionalTypeMismatchWithRegisteredConverter() {
        Map<String, Object> map = singletonMap("numbers", "whatever");

        mapToObjectConverter.registerConverter(int[].class, (SingleValueConverter) value -> new Integer[][] {{5}});

        expectedException.expect(RegisteredConverterException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'Optional<java.lang.Integer[][]>' returned by registered converter to field 'numbers' of type 'Optional<int[]>'."));

        mapToObjectConverter.convert(map, ClassWithOptionalPrimitiveArray.class);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Generic types fields

    private class ClassWithRawList {
        List list;
    }

    @Test
    public void usesRegisteredConverterForRawList() {
        Map<String, Object> map = singletonMap("list", null);

        mapToObjectConverter
                .registerConverter(List.class, value -> asList(3, 6, 9));

        ClassWithRawList actual = mapToObjectConverter.convert(map, ClassWithRawList.class);

        ClassWithRawList expected = new ClassWithRawList();
        expected.list = asList(3, 6, 9);

        assertObjectsEqual(actual, expected);
    }



    private class ClassWithListOfUnknownType {
        List<?> list;
    }

    @Test
    public void throwsExceptionOtherThanRegisteredConverterExceptionWhenRegisteredConverterHasDifferentGenericTypeThanTheField() {
        Map<String, Object> map = singletonMap("list", null);

        mapToObjectConverter
                .registerConverter(List.class, value -> asList(3, 6, 9));

        expectedException.expect(ConverterNullValueException.class);
        expectedException.expectMessage(equalTo("Null values require fields to be Optional. Null values for fields: 'list'."));

        mapToObjectConverter.convert(map, ClassWithListOfUnknownType.class);
    }



    private class ClassWithTwoLists {
        List<Integer> numbers;
        List<String> words;
    }

    @Test
    public void allowsToRegisterConvertersForGenericTypes() throws NoSuchFieldException {
        Map<String, Object> map = Map.of(
                "numbers", "whatever 1",
                "words", "whatever 2"
        );

        mapToObjectConverter
                .registerConverter(List.class, value -> {
                    throw new AssertionError("shouldn't be used");
                })
                .registerConverter(ClassWithTwoLists.class.getDeclaredField("numbers").getGenericType(), value -> asList(-5, 0, 5))
                .registerConverter(ClassWithTwoLists.class.getDeclaredField("words").getGenericType(), value -> asList("a", "b", "c"));

        ClassWithTwoLists actual = mapToObjectConverter.convert(map, ClassWithTwoLists.class);

        ClassWithTwoLists expected = new ClassWithTwoLists();
        expected.numbers = asList(-5, 0, 5);
        expected.words = asList("a", "b", "c");

        assertObjectsEqual(actual, expected);
    }

}
