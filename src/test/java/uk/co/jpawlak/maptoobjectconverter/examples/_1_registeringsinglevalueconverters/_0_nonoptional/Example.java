package uk.co.jpawlak.maptoobjectconverter.examples._1_registeringsinglevalueconverters._0_nonoptional;

import org.junit.Test;
import uk.co.jpawlak.maptoobjectconverter.MapToObjectConverter;
import uk.co.jpawlak.maptoobjectconverter.examples._1_registeringsinglevalueconverters.Gender;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class Example {

    @Test
    public void convertsMapToEmployee() {
        Map<String, Object> employeeMap = new HashMap<>();
        employeeMap.put("gender", 1);

        MapToObjectConverter converter = new MapToObjectConverter();
        converter.registerConverter(Gender.class, number -> Gender.fromInt((int) number));

        Employee employee = converter.convert(employeeMap, Employee.class);

        assertEquals(Gender.FEMALE, employee.gender);
    }

}
