package uk.co.jpawlak.maptoobjectconverter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;

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

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Null values require fields to be Optional. Null values for fields: 'x'"));

        mapToObjectConverter.convert(map, ClassWithEnumField.class);
    }

    @Test
    public void throwsExceptionWhenThereIsNoEnumForString() {
        Map<String, Object> map = singletonMap("x", "blah");

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("'uk.co.jpawlak.maptoobjectconverter.MapToObjectConverterTest_EnumFields$MyEnum' does not have an enum named 'blah'"));

        mapToObjectConverter.convert(map, ClassWithEnumField.class);
    }

    @Test
    public void throwsExceptionWhenTryingToConvertNonStringToEnum() {
        Map<String, Object> map = singletonMap("x", 123);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot convert value of type 'java.lang.Integer' to enum"));

        mapToObjectConverter.convert(map, ClassWithEnumField.class);
    }

    //TODO: test mapping to enum with fields - make sure that it doesn't set fields on the enum

    //TODO: optional - correct enum
    //TODO: optional - string with no enum for it
    //TODO: optional - trying to assign int value to enum field

    //TODO: what if someone wants to use other method than valueOf(String)? e.g. find enum by one of its fields?

}
