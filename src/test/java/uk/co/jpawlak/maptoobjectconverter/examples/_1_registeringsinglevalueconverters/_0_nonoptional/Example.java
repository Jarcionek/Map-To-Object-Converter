package uk.co.jpawlak.maptoobjectconverter.examples._1_registeringsinglevalueconverters._0_nonoptional;

import org.junit.Test;
import uk.co.jpawlak.maptoobjectconverter.MapToObjectConverter;
import uk.co.jpawlak.maptoobjectconverter.examples._1_registeringsinglevalueconverters.Gender;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;

public class Example {

    @Test
    public void convertsMapToEmployee() {
        Map<String, Object> employeeMap = singletonMap("gender", 1);

        MapToObjectConverter converter = new MapToObjectConverter();
        converter.registerConverter(Gender.class, number -> Gender.fromInt((int) number));

        Employee employee = converter.convert(employeeMap, Employee.class);

        assertEquals(Gender.FEMALE, employee.gender);
    }

    @Test
    public void convertsMapToEmployeeWithDefaultValue() {
        Map<String, Object> employeeMap = singletonMap("gender", null);

        MapToObjectConverter converter = new MapToObjectConverter();
        converter.registerConverter(Gender.class, number -> number == null ? Gender.MALE : Gender.fromInt((int) number));

        Employee employee = converter.convert(employeeMap, Employee.class);

        assertEquals(Gender.MALE, employee.gender);
    }

}
