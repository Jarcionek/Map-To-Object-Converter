package uk.co.jpawlak.maptoobjectconverter;

import com.google.common.collect.ImmutableMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;
import java.util.Optional;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

//@SuppressWarnings("unused") // classes declared here are used via reflection API
public class MapToObjectConverterTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

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



    public static class ClassWithBoxedPrimitiveDataTypes {
        Character _character;
        Boolean _boolean;
        Byte _byte;
        Short _short;
        Integer _integer;
        Long _long;
        Float _float;
        Double _double;
    }

    @Test
    public void handlesAllBoxedPrimitiveDataTypes() {
        Map<String, Object> map = ImmutableMap.<String, Object>builder()
                .put("_character", 'a')
                .put("_boolean", true)
                .put("_byte", (byte) 1)
                .put("_short", (short) 2)
                .put("_integer", 3)
                .put("_long", 4L)
                .put("_float", 0.5f)
                .put("_double", 0.25d)
                .build();

        ClassWithBoxedPrimitiveDataTypes actual = mapToObjectConverter.convert(map, ClassWithBoxedPrimitiveDataTypes.class);

        ClassWithBoxedPrimitiveDataTypes expected = new ClassWithBoxedPrimitiveDataTypes();
        expected._character = 'a';
        expected._boolean = true;
        expected._byte = 1;
        expected._short = 2;
        expected._integer = 3;
        expected._long = 4L;
        expected._float = 0.5f;
        expected._double = 0.25d;

        assertThat(actual, sameBeanAs(expected));
    }



    public static class ClassWithUnboxedPrimitiveDataTypes {
        char _character;
        boolean _boolean;
        byte _byte;
        short _short;
        int _integer;
        long _long;
        float _float;
        double _double;
    }

    @Test
    public void handlesAllUnboxedPrimitiveDataTypes() {
        Map<String, Object> map = ImmutableMap.<String, Object>builder()
                .put("_character", 'a')
                .put("_boolean", true)
                .put("_byte", (byte) 1)
                .put("_short", (short) 2)
                .put("_integer", 3)
                .put("_long", 4L)
                .put("_float", 0.5f)
                .put("_double", 0.25d)
                .build();

        ClassWithUnboxedPrimitiveDataTypes actual = mapToObjectConverter.convert(map, ClassWithUnboxedPrimitiveDataTypes.class);

        ClassWithUnboxedPrimitiveDataTypes expected = new ClassWithUnboxedPrimitiveDataTypes();
        expected._character = 'a';
        expected._boolean = true;
        expected._byte = 1;
        expected._short = 2;
        expected._integer = 3;
        expected._long = 4L;
        expected._float = 0.5f;
        expected._double = 0.25d;

        assertThat(actual, sameBeanAs(expected));
    }



    public static class ClassWithPrivateField {
        private String string;
    }

    @Test
    public void setsPrivateFields() {
        Map<String, Object> map = singletonMap("string", "value");

        ClassWithPrivateField actual = mapToObjectConverter.convert(map, ClassWithPrivateField.class);

        ClassWithPrivateField expected = new ClassWithPrivateField();
        expected.string = "value";

        assertThat(actual, sameBeanAs(expected));
    }



    public static class ClassWithFinalField {
        private final String string;
        public ClassWithFinalField(String string) {
            this.string = string;
        }
    }

    @Test
    public void setsFinalFields() {
        Map<String, Object> map = singletonMap("string", "value");

        ClassWithFinalField actual = mapToObjectConverter.convert(map, ClassWithFinalField.class);

        ClassWithFinalField expected = new ClassWithFinalField("value");

        assertThat(actual, sameBeanAs(expected));
    }



    private static class ClassWithNonOptionalField {
        String address;
    }

    @Test
    public void throwsExceptionWhenValueIsNull() {
        Map<String, Object> map = singletonMap("address", null);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("field 'address' was null, make it Optional");

        mapToObjectConverter.convert(map, ClassWithNonOptionalField.class);
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



    //TODO: support for jdbi specific types (Timestamp, what else?) - configurable?
    //TODO: mapping to boxed primitives for singleton maps
    //TODO: type safety for generics in optionals
    //TODO: number of fields = keySet().size()
    //TODO: class with inheritance
    //TODO: ignores static fields
    //TODO: ignores characters case in fields names (configurable)

}
