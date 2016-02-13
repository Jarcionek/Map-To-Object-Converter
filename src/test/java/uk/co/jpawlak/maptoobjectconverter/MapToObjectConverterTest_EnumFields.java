package uk.co.jpawlak.maptoobjectconverter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterEnumCreationException;
import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterNullValueException;

import java.util.Map;
import java.util.Optional;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;

@SuppressWarnings("unused")
public class MapToObjectConverterTest_EnumFields {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final MapToObjectConverter mapToObjectConverter = new MapToObjectConverter();



    private enum MyEnum {
        ONE, TWO, THREE
    }

    private static class ClassWithEnumField {
        MyEnum x;
    }

    @Test
    public void setsEnumField() {
        Map<String, Object> map = singletonMap("x", "ONE");

        ClassWithEnumField actual = mapToObjectConverter.convert(map, ClassWithEnumField.class);

        ClassWithEnumField expected = new ClassWithEnumField();
        expected.x = MyEnum.ONE;

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void throwsExceptionWhenValueIsNull() {
        Map<String, Object> map = singletonMap("x", null);

        expectedException.expect(ConverterNullValueException.class);
        expectedException.expectMessage(equalTo("Null values require fields to be Optional. Null values for fields: 'x'."));

        mapToObjectConverter.convert(map, ClassWithEnumField.class);
    }

    @Test
    public void throwsExceptionWhenThereIsNoEnumForString() {
        Map<String, Object> map = singletonMap("x", "blah");

        expectedException.expect(ConverterEnumCreationException.class);
        expectedException.expectMessage(equalTo("'uk.co.jpawlak.maptoobjectconverter.MapToObjectConverterTest_EnumFields$MyEnum' does not have an enum named 'blah'."));

        mapToObjectConverter.convert(map, ClassWithEnumField.class);
    }

    @Test
    public void throwsExceptionWhenTryingToConvertNonStringToEnum() {
        Map<String, Object> map = singletonMap("x", 123);

        expectedException.expect(ConverterEnumCreationException.class);
        expectedException.expectMessage(equalTo("Cannot convert value of type 'java.lang.Integer' to enum."));

        mapToObjectConverter.convert(map, ClassWithEnumField.class);
    }



    private static class ClassWithOptionalEnumField {
        Optional<MyEnum> x;
    }

    @Test
    public void setsOptionalEnumField() {
        Map<String, Object> map = singletonMap("x", "TWO");

        ClassWithOptionalEnumField actual = mapToObjectConverter.convert(map, ClassWithOptionalEnumField.class);

        ClassWithOptionalEnumField expected = new ClassWithOptionalEnumField();
        expected.x = Optional.of(MyEnum.TWO);

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void setsOptionalEmptyEnumFieldWhenValueIsNull() {
        Map<String, Object> map = singletonMap("x", null);

        ClassWithOptionalEnumField actual = mapToObjectConverter.convert(map, ClassWithOptionalEnumField.class);

        ClassWithOptionalEnumField expected = new ClassWithOptionalEnumField();
        expected.x = Optional.empty();

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void throwsExceptionWhenThereIsNoEnumForStringAndFieldIsOptional() {
        Map<String, Object> map = singletonMap("x", "blah");

        expectedException.expect(ConverterEnumCreationException.class);
        expectedException.expectMessage(equalTo("'uk.co.jpawlak.maptoobjectconverter.MapToObjectConverterTest_EnumFields$MyEnum' does not have an enum named 'blah'."));

        mapToObjectConverter.convert(map, ClassWithOptionalEnumField.class);
    }

    @Test
    public void throwsExceptionWhenTryingToConvertNonStringToEnumAndFieldIsOptional() {
        Map<String, Object> map = singletonMap("x", 123);

        expectedException.expect(ConverterEnumCreationException.class);
        expectedException.expectMessage(equalTo("Cannot convert value of type 'java.lang.Integer' to enum."));

        mapToObjectConverter.convert(map, ClassWithOptionalEnumField.class);
    }

}
