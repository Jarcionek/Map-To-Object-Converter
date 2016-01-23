package uk.co.jpawlak.maptoobjectconverter;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Map;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static java.util.Collections.emptyMap;

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



    public static class ClassWithMultipleFields {
        String one;
        String two;
        String three;
    }

    @Test
    public void convertsMapToObjectOfSpecifiedClassWithMultipleFields() {
        Map<String, Object> map = ImmutableMap.of(
                "one", "1",
                "two", "2",
                "three", "3"
        );

        ClassWithMultipleFields actual = mapToObjectConverter.convert(map, ClassWithMultipleFields.class);

        ClassWithMultipleFields expected = new ClassWithMultipleFields();
        expected.one = "1";
        expected.two = "2";
        expected.three = "3";

        assertThat(actual, sameBeanAs(expected));
    }



    public static class ClassWithNotWorkingConstructor {
        public ClassWithNotWorkingConstructor() {
            throw new AssertionError("Unexpected call of the constructor");
        }
    }

    @Test
    public void createsObjectWithoutCallingItsConstructor() {
        Map<String, Object> map = emptyMap();

        mapToObjectConverter.convert(map, ClassWithNotWorkingConstructor.class);
    }


    //TODO: sets final fields
    //TODO: support for various types (numbers - primitives and boxed, string)
    //TODO: support for jdbi specific types (Timestamp, what else?) - configurable?
    //TODO: mapping to boxed primitives for singleton maps
    //TODO: requires Optional field for null values
    //TODO: type safety for generics in optionals
    //TODO: number of fields = keySet().size()
    //TODO: class with inheritance
    //TODO: ignores static fields
    //TODO: ignores characters case in fields names (configurable)

}
