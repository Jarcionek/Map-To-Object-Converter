package uk.co.jpawlak.maptoobjectconverter.examples._0_basicusage;

import org.junit.Test;
import uk.co.jpawlak.maptoobjectconverter.MapToObjectConverter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class Example {

    @Test
    public void convertsMapToEmployee() {
        Map<String, Object> employeeMap = new HashMap<>();
        employeeMap.put("name", "Jaroslaw Pawlak");
        employeeMap.put("age", 26);
        employeeMap.put("gender", "MALE");
        employeeMap.put("phoneNumber", null);

        MapToObjectConverter converter = new MapToObjectConverter();

        Employee employee = converter.convert(employeeMap, Employee.class);

        assertEquals("Jaroslaw Pawlak", employee.name);
        assertEquals(26, employee.age);
        assertEquals(Gender.MALE, employee.gender);
        assertEquals(Optional.empty(), employee.phoneNumber);
        // multiple assertions give poor diagnostics, use shazamcrest instead
    }

}
