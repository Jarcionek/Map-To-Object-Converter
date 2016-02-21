package uk.co.jpawlak.maptoobjectconverter.examples._0_basicusage;

import java.util.Optional;

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
