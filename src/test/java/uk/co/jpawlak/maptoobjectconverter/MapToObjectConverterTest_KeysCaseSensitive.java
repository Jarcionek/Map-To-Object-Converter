package uk.co.jpawlak.maptoobjectconverter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterMissingFieldsException;
import uk.co.jpawlak.maptoobjectconverter.exceptions.ConverterMissingValuesException;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static uk.co.jpawlak.maptoobjectconverter.TestUtil.assertObjectsEqual;

public class MapToObjectConverterTest_KeysCaseSensitive {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final MapToObjectConverter converter = new MapToObjectConverter(true);



    private static class SimpleClass {
        String tExT;
        String teXt;
    }

    @Test
    public void assignsToFieldsConsideringCase() {
        Map<String, Object> map = Map.of(
                "tExT", "1",
                "teXt", "2"
        );

        SimpleClass actual = converter.convert(map, SimpleClass.class);

        SimpleClass expected = new SimpleClass();
        expected.tExT = "1";
        expected.teXt = "2";

        assertObjectsEqual(actual, expected);
    }


    @Test
    public void throwsExceptionWhenFewerKeysThanFields() {
        Map<String, Object> map = singletonMap("teXt", "1");

        expectedException.expect(ConverterMissingValuesException.class);
        expectedException.expectMessage(equalTo("No values for fields: 'tExT'."));

        converter.convert(map, SimpleClass.class);
    }

    @Test
    public void throwsExceptionWhenMoreKeysThanFields() {
        Map<String, Object> map = Map.of(
                "tExT", "1",
                "teXt", "2",
                "text", "3"
        );

        expectedException.expect(ConverterMissingFieldsException.class);
        expectedException.expectMessage(equalTo("No fields for keys: 'text'."));

        converter.convert(map, SimpleClass.class);
    }

}
