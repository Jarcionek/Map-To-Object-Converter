package uk.co.jpawlak.maptoobjectconverter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterFieldDuplicateException;

import java.util.Map;

import static com.shazam.shazamcrest.MatcherAssert.assertThat;
import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static java.util.Collections.emptyMap;
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
        Map<String, Object> map = emptyMap();

        expectedException.expect(ConverterFieldDuplicateException.class);
        expectedException.expectMessage(equalTo("Fields 'one', 'oNe', 'onE', 'two', 'twO' are duplicates (converter is key case insensitive)."));

        converter.convert(map, FieldDuplications.class);
    }

}
