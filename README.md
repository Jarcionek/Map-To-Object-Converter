# Map-To-Object-Converter
A tool that allows to easily convert `Map<String, Object>` returned by jdbi (but not only) into staticly typed classes.

Example:
```java
public enum Gender {
    MALE, FEMALE
}
 
public class Employee {
 
    public final String firstName;
    public final String surname;
    public final int age;
    public final Gender gender;
    public final Optional<String> phoneNumber;
 
    public Employee(String firstName, String surname, int age, Gender gender, Optional<String> phoneNumber) {
        this.firstName = firstName;
        this.surname = surname;
        this.age = age;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
    }
 
}
 
import org.junit.Assert;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import static org.junit.Assert.assertEquals;
 
public class Example {
 
    @Test
    public void convertsMapToEmployee() {
        Map<String, Object> employeeMap = new HashMap<>();
        employeeMap.put("firstName", "Jaroslaw");
        employeeMap.put("surname", "Pawlak");
        employeeMap.put("age", 26);
        employeeMap.put("gender", "MALE");
        employeeMap.put("phoneNumber", null);
 
        MapToObjectConverter converter = new MapToObjectConverter();
 
        Employee employee = converter.convert(employeeMap, Employee.class);
 
        assertEquals("Jaroslaw", employee.firstName);
        assertEquals("Pawlak", employee.surname);
        assertEquals(26, employee.age);
        assertEquals(Gender.MALE, employee.gender);
        assertEquals(Optional.empty(), employee.phoneNumber);
        // multiple assertions give poor diagnostics, use shazamcrest instead
    }
 
}
```

Features:
* assigns the values from the map to fields whose names are equal to the keys
* throws exception if there are entries without corresponding fields (of course listing the keys)
* throws exception if there are fields for which there were no values (of course listing names of all such fields)
* assigns the fields regardless of their access modifier and final keyword – no methods or annotations are required in the class
* creates the instance of the class without calling its constructor so you have a complete freedom in how you want to define the class
* differs between no value for key or value being null
* requires the field to be `Optional` if the value is null, so once we have the staticly typed class, we know there aren’t any nulls
* it checks the type of field and value, also in case of `Optional` fields
* can map String values to enums (using static `valueOf(String)` method)
* allows the fields to be supertypes of values, so you can assign `Integer` value to `Number` field
* unfortunately, it doesn’t allow wildcards in `Optionals`, so `Integer` value can be assigned to `Optional<Integer>` field but cannot be assigned to field declared as `Optional<? extends Number>` (this might be improved in future)
* doesn’t allow raw Optionals
