package uk.co.jpawlak.maptoobjectconverter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterIllegalArgumentException;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.co.jpawlak.maptoobjectconverter.TestUtil.assertObjectsEqual;

@SuppressWarnings("unused")
public class MapToObjectConverterTest_KeysCaseInsensitive {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final MapToObjectConverter converter = new MapToObjectConverter(false);



    private static class SimpleClass {
        String propertyName;
    }

    @Test
    public void convertsMapToObjectIgnoringTheCase() {
        Map<String, Object> map = singletonMap("PrOpErTyNaMe", "stringValue");

        SimpleClass actual = converter.convert(map, SimpleClass.class);

        SimpleClass expected = new SimpleClass();
        expected.propertyName = "stringValue";

        assertObjectsEqual(actual, expected);
    }



    private static class FieldDuplications {
        String one;
        String oNe;
        String onE;
        String two;
        String twO;
        String three;
    }

    @Test
    public void setsAllFieldsOfTheSameName() {
        Map<String, Object> map = Map.of(
            "oNe", "1",
            "Two", "2",
            "thREE", "3"
        );

        FieldDuplications actual = converter.convert(map, FieldDuplications.class);

        FieldDuplications expected = new FieldDuplications();
        expected.one = "1";
        expected.oNe = "1";
        expected.onE = "1";
        expected.two = "2";
        expected.twO = "2";
        expected.three = "3";

        assertObjectsEqual(actual, expected);
    }

    @Test
    public void throwsExceptionWhenMapHasMultipleKeysWhichAreEqualIgnoringCase() {
        Map<String, Object> map = Map.ofEntries(
                Map.entry("one", 0),
                Map.entry("oNe", 0),
                Map.entry("onE", 0),
                Map.entry("two", 0),
                Map.entry("twO", 0),
                Map.entry("three", 0)
        );

        expectedException.expect(ConverterIllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Keys 'one', 'oNe', 'onE', 'two', 'twO' are duplicates (converter is key case insensitive)."));

        converter.convert(map, SimpleClass.class);
    }



    private static class FieldDuplicationsWithParentClass extends FieldDuplications {
        String oNe;
        private String getSuperONe() {
            return super.oNe;
        }
    }

    @Test
    public void setsAllFieldsOfTheSameNameIfThereAreDuplicatedInSuperClasses() {
        Map<String, Object> map = Map.of(
                "oNe", "1",
                "Two", "2",
                "thREE", "3"
        );

        FieldDuplicationsWithParentClass actual = converter.convert(map, FieldDuplicationsWithParentClass.class);

        assertThat(actual.one, equalTo("1"));
        assertThat(actual.onE, equalTo("1"));
        assertThat(actual.oNe, equalTo("1"));
        assertThat(actual.getSuperONe(), equalTo("1"));
    }

}
