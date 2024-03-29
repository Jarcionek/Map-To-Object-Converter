package uk.co.jpawlak.maptoobjectconverter;

import org.hamcrest.MatcherAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterIllegalArgumentException;
import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterMissingFieldsException;
import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterMissingValuesException;
import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterTypeMismatchException;

import java.util.Hashtable;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.co.jpawlak.maptoobjectconverter.TestUtil.assertObjectsEqual;

@SuppressWarnings({"SameParameterValue", "unused", "WeakerAccess"})
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

        assertObjectsEqual(actual, expected);
    }

    @Test
    public void throwsExceptionForTypeMismatch() {
        Map<String, Object> map = singletonMap("propertyName", 3);

        expectedException.expect(ConverterTypeMismatchException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'java.lang.Integer' to field 'propertyName' of type 'java.lang.String'."));

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

        expectedException.expect(ConverterTypeMismatchException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'java.lang.Character' to field 'trueOrFalse' of type 'boolean'."));

        mapToObjectConverter.convert(map, SimpleClassWithPrimitiveBooleanField.class);
    }

    @Test
    public void throwsExceptionForPrimitiveTypeMismatch_assigningBoolean() {
        Map<String, Object> map = singletonMap("number", true);

        expectedException.expect(ConverterTypeMismatchException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'java.lang.Boolean' to field 'number' of type 'int'."));

        mapToObjectConverter.convert(map, SimpleClassWithPrimitiveField.class);
    }

    @Test
    public void throwsExceptionForPrimitiveTypeMismatch_assigningByte() {
        Map<String, Object> map = singletonMap("trueOrFalse", (byte) 1);

        expectedException.expect(ConverterTypeMismatchException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'java.lang.Byte' to field 'trueOrFalse' of type 'boolean'."));

        mapToObjectConverter.convert(map, SimpleClassWithPrimitiveBooleanField.class);
    }

    @Test
    public void throwsExceptionForPrimitiveTypeMismatch_assigningShort() {
        Map<String, Object> map = singletonMap("trueOrFalse", (short) 2);

        expectedException.expect(ConverterTypeMismatchException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'java.lang.Short' to field 'trueOrFalse' of type 'boolean'."));

        mapToObjectConverter.convert(map, SimpleClassWithPrimitiveBooleanField.class);
    }

    @Test
    public void throwsExceptionForPrimitiveTypeMismatch_assigningInteger() {
        Map<String, Object> map = singletonMap("trueOrFalse", 3);

        expectedException.expect(ConverterTypeMismatchException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'java.lang.Integer' to field 'trueOrFalse' of type 'boolean'."));

        mapToObjectConverter.convert(map, SimpleClassWithPrimitiveBooleanField.class);
    }

    @Test
    public void throwsExceptionForPrimitiveTypeMismatch_assigningLong() {
        Map<String, Object> map = singletonMap("number", 4L);

        expectedException.expect(ConverterTypeMismatchException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'java.lang.Long' to field 'number' of type 'int'."));

        mapToObjectConverter.convert(map, SimpleClassWithPrimitiveField.class);
    }

    @Test
    public void throwsExceptionForPrimitiveTypeMismatch_assigningFloat() {
        Map<String, Object> map = singletonMap("number", 0.5f);

        expectedException.expect(ConverterTypeMismatchException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'java.lang.Float' to field 'number' of type 'int'."));

        mapToObjectConverter.convert(map, SimpleClassWithPrimitiveField.class);
    }

    @Test
    public void throwsExceptionForPrimitiveTypeMismatch_assigningDouble() {
        Map<String, Object> map = singletonMap("number", 0.25d);

        expectedException.expect(ConverterTypeMismatchException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'java.lang.Double' to field 'number' of type 'int'."));

        mapToObjectConverter.convert(map, SimpleClassWithPrimitiveField.class);
    }



    public static class ClassWithMultipleFields {
        String one;
        String two;
        String three;
    }

    @Test
    public void convertsMapToObjectOfSpecifiedClassWithMultipleFields() {
        Map<String, Object> map = Map.of(
                "one", "1",
                "two", "2",
                "three", "3"
        );

        ClassWithMultipleFields actual = mapToObjectConverter.convert(map, ClassWithMultipleFields.class);

        ClassWithMultipleFields expected = new ClassWithMultipleFields();
        expected.one = "1";
        expected.two = "2";
        expected.three = "3";

        assertObjectsEqual(actual, expected);
    }

    @Test
    public void throwsExceptionWhenMoreKeysThanFields() {
        Map<String, Object> map = Map.of(
                "one", "1",
                "two", "2",
                "three", "3",
                "four", "4",
                "five", "5"
        );

        expectedException.expect(ConverterMissingFieldsException.class);
        expectedException.expectMessage(equalTo("No fields for keys: 'four', 'five'."));

        mapToObjectConverter.convert(map, ClassWithMultipleFields.class);
    }

    @Test
    public void throwsExceptionWhenFewerKeysThanFields() {
        Map<String, Object> map = singletonMap("one", "1");

        expectedException.expect(ConverterMissingValuesException.class);
        expectedException.expectMessage(equalTo("No values for fields: 'two', 'three'."));

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
        Map<String, Object> map = Map.ofEntries(
                Map.entry("_character", 'a'),
                Map.entry("_boolean", true),
                Map.entry("_byte", (byte) 1),
                Map.entry("_short", (short) 2),
                Map.entry("_integer", 3),
                Map.entry("_long", 4L),
                Map.entry("_float", 0.5f),
                Map.entry("_double", 0.25d)
        );

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

        assertObjectsEqual(actual, expected);
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
        Map<String, Object> map = Map.ofEntries(
                Map.entry("_character", 'a'),
                Map.entry("_boolean", true),
                Map.entry("_byte", (byte) 1),
                Map.entry("_short", (short) 2),
                Map.entry("_integer", 3),
                Map.entry("_long", 4L),
                Map.entry("_float", 0.5f),
                Map.entry("_double", 0.25d)
        );

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

        assertObjectsEqual(actual, expected);
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

        assertObjectsEqual(actual, expected);
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

        assertObjectsEqual(actual, expected);
    }



    private static class ParentClass {
        int a;
    }

    private static class ChildClass extends ParentClass {
        int b;
    }

    @Test
    public void considersFieldsInSuperClasses() {
        Map<String, Object> map = Map.of(
                "a", 1,
                "b", 2
        );

        ChildClass actual = mapToObjectConverter.convert(map, ChildClass.class);

        ChildClass expected = new ChildClass();
        expected.a = 1;
        expected.b = 2;

        assertObjectsEqual(actual, expected);
    }



    private static class ChildClassWithDuplicatedField extends ParentClass {
        int a;
        public int getSuperA() {
            return super.a;
        }
    }

    @Test
    public void setsAllFieldsOfTheSameNameIfThereAreDuplicatedInSuperClasses() {
        Map<String, Object> map = Map.of(
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
        Map<String, Object> map = Map.of(
                "a", 7
        );

        expectedException.expect(ConverterTypeMismatchException.class);
        expectedException.expectMessage(equalTo("Cannot assign value of type 'java.lang.Integer' to field 'a' of type 'java.lang.String'."));

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

        assertObjectsEqual(actual, expected);
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

        expectedException.expect(ConverterIllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot convert map to abstract class."));

        mapToObjectConverter.convert(map, AbstractClass.class);
    }

    @Test
    public void throwsExceptionWhenMapIsNull() {
        expectedException.expect(ConverterIllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Map cannot be null."));

        mapToObjectConverter.convert(null, SimpleClass.class);
    }

    @Test
    public void throwsExceptionWhenKeyIsNull() {
        expectedException.expect(ConverterIllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Map's keys cannot be null."));

        mapToObjectConverter.convert(singletonMap(null, "string"), SimpleClass.class);
    }

    @Test
    public void doesNotThrowsExceptionForValidMapWhichDoesNotPermitNullKeys() {
        Map<String, Object> map = new Hashtable<>();
        map.put("propertyName", "string");

        mapToObjectConverter.convert(map, SimpleClass.class);
    }

    @Test
    public void throwsExceptionWhenTargetClassIsNull() {
        Map<String, Object> map = singletonMap("a", 3);

        expectedException.expect(ConverterIllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Target class cannot be null."));

        mapToObjectConverter.convert(map, null);
    }

    @Test
    public void throwsExceptionWhenTargetClassIsPrimitive() {
        Map<String, Object> map = singletonMap("a", 3);

        expectedException.expect(ConverterIllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot convert map to primitive type."));

        mapToObjectConverter.convert(map, int.class);
    }

    private enum Enum {
        X;
        int a;
    }

    @Test
    public void throwsExceptionWhenTargetClassIsEnum() {
        Map<String, Object> map = singletonMap("a", 3);

        expectedException.expect(ConverterIllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot convert map to enum."));

        mapToObjectConverter.convert(map, Enum.class);
    }

    private interface Interface {}

    @Test
    public void throwsExceptionWhenTargetClassIsInterface() {
        Map<String, Object> map = singletonMap("a", 3);

        expectedException.expect(ConverterIllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot convert map to interface."));

        mapToObjectConverter.convert(map, Interface.class);
    }

    private @interface Annotation {}

    @Test
    public void throwsExceptionWhenTargetClassIsAnnotation() {
        Map<String, Object> map = singletonMap("a", 3);

        expectedException.expect(ConverterIllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Cannot convert map to annotation."));

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

        assertObjectsEqual(actual, expected);
    }

}
