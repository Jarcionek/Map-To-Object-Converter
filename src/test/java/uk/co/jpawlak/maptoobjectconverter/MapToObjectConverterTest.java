package uk.co.jpawlak.maptoobjectconverter;

import com.google.common.collect.ImmutableMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;

@SuppressWarnings({"SameParameterValue", "unused"})
public class MapToObjectConverterTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final MapToObjectConverter mapToObjectConverter = new MapToObjectConverter();



    public static class SimpleClass {
        String propertyName;
    }

    @Test
    public void convertsMapToObjectOfSpecifiedClass() {
        Map<String, Object> map = singletonMap("propertyName", "stringValue");

        SimpleClass actual = mapToObjectConverter.convert(map, SimpleClass.class);

        SimpleClass expected = new SimpleClass();
        expected.propertyName = "stringValue";

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void throwsExceptionForTypeMismatch() {
        Map<String, Object> map = singletonMap("propertyName", 3);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'java.lang.Integer' to field 'propertyName' of type 'java.lang.String'"));

        mapToObjectConverter.convert(map, SimpleClass.class);
    }



    private static class SimpleClassWithPrimitiveField {
        int number;
    }

    private static class SimpleClassWithPrimitiveBooleanField {
        boolean trueOrFalse;
    }

    @Test
    public void throwsExceptionForPrimitiveTypeMismatch_assigningChar() {
        Map<String, Object> map = singletonMap("trueOrFalse", 'x');

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'java.lang.Character' to field 'trueOrFalse' of type 'boolean'"));

        mapToObjectConverter.convert(map, SimpleClassWithPrimitiveBooleanField.class);
    }

    @Test
    public void throwsExceptionForPrimitiveTypeMismatch_assigningBoolean() {
        Map<String, Object> map = singletonMap("number", true);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'java.lang.Boolean' to field 'number' of type 'int'"));

        mapToObjectConverter.convert(map, SimpleClassWithPrimitiveField.class);
    }

    @Test
    public void throwsExceptionForPrimitiveTypeMismatch_assigningByte() {
        Map<String, Object> map = singletonMap("trueOrFalse", (byte) 1);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'java.lang.Byte' to field 'trueOrFalse' of type 'boolean'"));

        mapToObjectConverter.convert(map, SimpleClassWithPrimitiveBooleanField.class);
    }

    @Test
    public void throwsExceptionForPrimitiveTypeMismatch_assigningShort() {
        Map<String, Object> map = singletonMap("trueOrFalse", (short) 2);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'java.lang.Short' to field 'trueOrFalse' of type 'boolean'"));

        mapToObjectConverter.convert(map, SimpleClassWithPrimitiveBooleanField.class);
    }

    @Test
    public void throwsExceptionForPrimitiveTypeMismatch_assigningInteger() {
        Map<String, Object> map = singletonMap("trueOrFalse", 3);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'java.lang.Integer' to field 'trueOrFalse' of type 'boolean'"));

        mapToObjectConverter.convert(map, SimpleClassWithPrimitiveBooleanField.class);
    }

    @Test
    public void throwsExceptionForPrimitiveTypeMismatch_assigningLong() {
        Map<String, Object> map = singletonMap("number", 4L);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'java.lang.Long' to field 'number' of type 'int'"));

        mapToObjectConverter.convert(map, SimpleClassWithPrimitiveField.class);
    }

    @Test
    public void throwsExceptionForPrimitiveTypeMismatch_assigningFloat() {
        Map<String, Object> map = singletonMap("number", 0.5f);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'java.lang.Float' to field 'number' of type 'int'"));

        mapToObjectConverter.convert(map, SimpleClassWithPrimitiveField.class);
    }

    @Test
    public void throwsExceptionForPrimitiveTypeMismatch_assigningDouble() {
        Map<String, Object> map = singletonMap("number", 0.25d);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'java.lang.Double' to field 'number' of type 'int'"));

        mapToObjectConverter.convert(map, SimpleClassWithPrimitiveField.class);
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

    @Test
    public void throwsExceptionWhenMoreKeysThanFields() {
        Map<String, Object> map = ImmutableMap.of(
                "one", "1",
                "two", "2",
                "three", "3",
                "four", "4",
                "five", "5"
        );

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("No fields for keys: 'four', 'five'"));

        mapToObjectConverter.convert(map, ClassWithMultipleFields.class);
    }

    @Test
    public void throwsExceptionWhenFewerKeysThanFields() {
        Map<String, Object> map = singletonMap("one", "1");

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("No values for fields: 'two', 'three'"));

        mapToObjectConverter.convert(map, ClassWithMultipleFields.class);
    }



    public static class ClassWithNotWorkingConstructor {
        int a;
        public ClassWithNotWorkingConstructor() {
            throw new AssertionError("Unexpected call of the constructor");
        }
    }

    @Test
    public void createsObjectWithoutCallingItsConstructor() {
        Map<String, Object> map = singletonMap("a", 7);

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
    public void setsBoxedPrimitiveFields() {
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
    public void setsUnboxedPrimitiveFields() {
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



    private static class ParentClass {
        int a;
    }

    private static class ChildClass extends ParentClass {
        int b;
    }

    @Test
    public void considersFieldsInSuperClasses() {
        Map<String, Object> map = ImmutableMap.of(
                "a", 1,
                "b", 2
        );

        ChildClass actual = mapToObjectConverter.convert(map, ChildClass.class);

        ChildClass expected = new ChildClass();
        expected.a = 1;
        expected.b = 2;

        assertThat(actual, sameBeanAs(expected));
    }



    private static class ChildClassWithDuplicatedField extends ParentClass {
        int a;
        public int getSuperA() {
            return super.a;
        }
    }

    @Test
    public void setsAllFieldsOfTheSameNameIfThereAreDuplicatedInSuperClasses() {
        Map<String, Object> map = ImmutableMap.of(
                "a", 7
        );

        ChildClassWithDuplicatedField actual = mapToObjectConverter.convert(map, ChildClassWithDuplicatedField.class);

        assertThat(actual.a, equalTo(7));
        assertThat(actual.getSuperA(), equalTo(7));
    }



    private static class ChildClassWithDuplicatedFieldButOfDifferentType extends ParentClass {
        String a;
    }

    @Test
    public void throwsTypeMismatchExceptionWhenChildAndParentClassesHaveFieldOfTheSameNameButOfDifferentType() {
        Map<String, Object> map = ImmutableMap.of(
                "a", 7
        );

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'java.lang.Integer' to field 'a' of type 'java.lang.String'"));

        mapToObjectConverter.convert(map, ChildClassWithDuplicatedFieldButOfDifferentType.class);
    }



    private static class ClassWithStaticField {
        private static int x;
        private int y;
    }

    @Test
    public void ignoresStaticFields() {
        Map<String, Object> map = singletonMap("y", 4);

        ClassWithStaticField actual = mapToObjectConverter.convert(map, ClassWithStaticField.class);

        ClassWithStaticField expected = new ClassWithStaticField();
        expected.y = 4;

        assertThat(actual, sameBeanAs(expected));
    }



    private static class ClassWithSyntheticField {
        // nested class will cause the compiler to create a synthetic field in ClassWithSyntheticField.Inner that represents the enclosing class ClassWithSyntheticField
        private class Inner {
            int a;
        }
    }

    @Test
    public void ignoresSyntheticFields() {
        Map<String, Object> map = singletonMap("a", 2);

        mapToObjectConverter.convert(map, ClassWithSyntheticField.Inner.class);
    }

    // invalid input ///////////////////////////////////////////////////////////////////////////////////////////////////

    private static abstract class AbstractClass {
        int a;
    }

    @Test
    public void throwsExceptionWhenTargetClassIsAbstract() {
        Map<String, Object> map = singletonMap("a", 3);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot convert map to abstract class"));

        mapToObjectConverter.convert(map, AbstractClass.class);
    }

    @Test
    public void throwsExceptionWhenMapIsNull() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Map cannot be null"));

        mapToObjectConverter.convert(null, SimpleClass.class);
    }

    @Test
    public void throwsExceptionWhenTargetClassIsNull() {
        Map<String, Object> map = singletonMap("a", 3);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Target class cannot be null"));

        mapToObjectConverter.convert(map, null);
    }

    @Test
    public void throwsExceptionWhenTargetClassIsPrimitive() {
        Map<String, Object> map = singletonMap("a", 3);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot convert map to primitive type. Use boxed primitive instead"));

        mapToObjectConverter.convert(map, int.class);
    }

    private enum Enum {
        X;
        int a;
    }

    @Test
    public void throwsExceptionWhenTargetClassIsEnum() {
        Map<String, Object> map = singletonMap("a", 3);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot convert map to enum"));

        mapToObjectConverter.convert(map, Enum.class);
    }

    private interface Interface {}

    @Test
    public void throwsExceptionWhenTargetClassIsInterface() {
        Map<String, Object> map = singletonMap("a", 3);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot convert map to interface"));

        mapToObjectConverter.convert(map, Interface.class);
    }

    private @interface Annotation {}

    @Test
    public void throwsExceptionWhenTargetClassIsAnnotation() {
        Map<String, Object> map = singletonMap("a", 3);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot convert map to annotation"));

        mapToObjectConverter.convert(map, Annotation.class);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static class ClassWithAbstractField {
        Number x;
    }

    @Test
    public void setsFieldOfDifferentTypeThanValueIfItIsAssignable() {
        Map<String, Object> map = singletonMap("x", 5);

        ClassWithAbstractField actual = mapToObjectConverter.convert(map, ClassWithAbstractField.class);

        ClassWithAbstractField expected = new ClassWithAbstractField();
        expected.x = 5;

        assertThat(actual, sameBeanAs(expected));
    }

}
