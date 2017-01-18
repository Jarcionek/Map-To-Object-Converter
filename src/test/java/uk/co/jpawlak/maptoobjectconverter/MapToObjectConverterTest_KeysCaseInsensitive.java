package uk.co.jpawlak.maptoobjectconverter;

import com.google.common.collect.ImmutableMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterIllegalArgumentException;

import java.util.Map;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;

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

        assertThat(actual, sameBeanAs(expected));
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
    public void throwsExceptionWhenMultipleFieldsHaveSameNameButDifferentCase() {
        Map<String, Object> map = ImmutableMap.<String, Object>builder()
            .put("oNe", "1")
            .put("Two", "2")
            .put("thREE", "3")
            .build();

        FieldDuplications actual = converter.convert(map, FieldDuplications.class);

        FieldDuplications expected = new FieldDuplications();
        expected.one = "1";
        expected.oNe = "1";
        expected.onE = "1";
        expected.two = "2";
        expected.twO = "2";
        expected.three = "3";

        assertThat(actual, sameBeanAs(expected));
    }

    @Test
    public void throwsExceptionWhenMapHasMultipleKeysWhichAreEqualIgnoringCase() {
        Map<String, Object> map = ImmutableMap.<String, Object>builder()
                .put("one", 0)
                .put("oNe", 0)
                .put("onE", 0)
                .put("two", 0)
                .put("twO", 0)
                .put("three", 0)
                .build();

        expectedException.expect(ConverterIllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Keys 'one', 'oNe', 'onE', 'two', 'twO' are duplicates (converter is key case insensitive)."));

        converter.convert(map, SimpleClass.class);
    }

}
