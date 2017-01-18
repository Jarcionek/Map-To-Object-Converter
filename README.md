# Map-To-Object-Converter

A tool that allows to easily convert `Map<String, Object>` returned by jdbi (but not only) into staticly typed objects.

## Examples

#### Basic usage

```java
public enum Gender {
    MALE, FEMALE
}

public class Employee {
    public final String name;
    public final int age;
    public final Gender gender;
    public final Optional<String> phoneNumber;

    public Employee(String name, int age, Gender gender, Optional<String> phoneNumber) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
    }
}

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
```

#### Registering single value converters for non-optional fields

But what if gender in our database is integer value, where 0 means male and 1 means female? We do not want to have
`int` field in our code, we still want to have `Gender` enum. In such cases we would usually add a method to our
`Gender` class:

```java
public enum Gender {
    MALE(0),
    FEMALE(1);

    private final int number;

    Gender(int number) {
        this.number = number;
    }

    public static Gender fromInt(int number) {
        return stream(Gender.values())
                .filter(enumValue -> enumValue.number == number)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No enum for number " + number));
    }
}
```

Now all we have to do is to register this method on the converter:

``` java
public class Employee {
    public Gender gender;
}

public class Example {
        
    @Test
    public void convertsMapToEmployee() {
        Map<String, Object> employeeMap = singletonMap("gender", 1);

        MapToObjectConverter converter = new MapToObjectConverter();
        converter.registerConverter(Gender.class, number -> Gender.fromInt((int) number));

        Employee employee = converter.convert(employeeMap, Employee.class);

        assertEquals(Gender.FEMALE, employee.gender);
    }

}
```

#### Registering single value converters for optional fields

Registering converts for optional fields works in the same way - we register converter for `Gender.class`, not for `Optional.class`.
The registered converter has to handle null, but a value returned by it will be automatically wrapped into `Optional`:

``` java
public class Employee {
    public final Optional<Gender> gender;

    public Employee(Gender gender) {
        this.gender = Optional.ofNullable(gender);
    }
}

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
```

#### Key case insensitive mode

By default, keys of the map and fields' name are case sensitive, so if the map contains two keys `abc` and `aBC`, and target class
contains two fields with names `abc` and `aBC`, the conversion will be successful:

``` java
public class Data {

    public int abc;
    public int aBC;

}

public class Example {

    @Test
    public void convertsToObjectInCaseSensitiveMode() {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("abc", 1);
        dataMap.put("aBC", 2);

        MapToObjectConverter converter = new MapToObjectConverter(true); // keyCaseSensitive = true (default)

        Data data = converter.convert(dataMap, Data.class);

        assertEquals(1, data.abc);
        assertEquals(2, data.aBC);
        // multiple assertions give poor diagnostics, use shazamcrest instead
    }
    
}
    
```

However, in key case insensitive mode this will throw the exception as keys `abc` and `aBC` are considered duplicates:

``` java
public class Example {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void throwsExceptionForDuplicateKeysInCaseInsensitiveMode() {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("abc", 1);
        dataMap.put("aBC", 2);

        MapToObjectConverter converter = new MapToObjectConverter(false); // keyCaseSensitive = false

        expectedException.expect(ConverterIllegalArgumentException.class);
        expectedException.expectMessage(equalTo("Keys 'abc', 'aBC' are duplicates (converter is key case insensitive)."));

        converter.convert(dataMap, Data.class);
    }
    
}
```

Key case insensitive mode allows to ignore the casing of fields and keys when converting map to object. In key case sensitive mode
the below example would throw the exception, as the map is missing values for fields `abc` and `aBC` while the target class
is missing a field for key `ABC`.

``` java
public class Example {

    @Test
    public void convertsToObjectInCaseInsensitiveMode() {
        Map<String, Object> dataMap = singletonMap("ABC", 7);

        MapToObjectConverter converter = new MapToObjectConverter(false); // keyCaseSensitive = false

        Data data = converter.convert(dataMap, Data.class);

        assertEquals(7, data.abc);
        assertEquals(7, data.aBC);
        // multiple assertions give poor diagnostics, use shazamcrest instead
    }

}
```

## Features
* assigns the values from the map to fields whose names are equal to the keys
* throws exception if there are entries without corresponding fields (of course listing the keys)
* throws exception if there are fields for which there were no values (of course listing names of all such fields)
* assigns the fields regardless of their access modifier and final keyword – no methods or annotations are required in the class
* creates the instance of the class without calling its constructor so you have a complete freedom in how you want to define the class
* differs between no value for key or value being null
* requires the field to be `Optional` if the value is null, so once we have the staticly typed class, we know there are no nulls
* it checks the type of field and value, also in case of `Optional` fields
* can map String values to enums (using static `valueOf(String)` method)
* allows to register type converters so you can convert String to enum using different method than `valueOf`, or you can convert int to enum
* allows the fields to be supertypes of values, so you can assign `Integer` value to `Number` field
* allows to ignore case of keys in the map
* unfortunately, it doesn’t allow wildcards in `Optionals`, so `Integer` value can be assigned to `Optional<Integer>` field but cannot be assigned to field declared as `Optional<? extends Number>` (this might be improved in future)
* doesn’t allow raw Optionals

## Quick start

Just add a Maven dependency to your pom file:

```
<dependency>
    <groupId>uk.co.jpawlak</groupId>
    <artifactId>map-to-object-converter</artifactId>
    <version>3.0</version>
</dependency>
```
