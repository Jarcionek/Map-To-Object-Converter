package uk.co.jpawlak.maptoobjectconverter.examples._1_registeringsinglevalueconverters._1_optional;

import org.junit.Test;
import uk.co.jpawlak.maptoobjectconverter.MapToObjectConverter;
import uk.co.jpawlak.maptoobjectconverter.examples._1_registeringsinglevalueconverters.Gender;

import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;

public class Example {

    @Test
    public void convertsMapToEmployeeWithNonNullOptionalField() {
        Map<String, Object> employeeMap = singletonMap("gender", 0);

        MapToObjectConverter converter = new MapToObjectConverter();
        converter.registerConverter(Gender.class, number -> number == null ? null : Gender.fromInt((int) number));

        Employee employee = converter.convert(employeeMap, Employee.class);

        assertEquals(Optional.of(Gender.MALE), employee.gender);
    }

    @Test
    public void convertsMapToEmployeeNullOptionalField() {
        Map<String, Object> employeeMap = singletonMap("gender", null);

        MapToObjectConverter converter = new MapToObjectConverter();
        converter.registerConverter(Gender.class, number -> number == null ? null : Gender.fromInt((int) number));

        Employee employee = converter.convert(employeeMap, Employee.class);

        assertEquals(Optional.empty(), employee.gender);
    }

}
