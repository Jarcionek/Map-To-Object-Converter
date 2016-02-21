package uk.co.jpawlak.maptoobjectconverter.examples._1_registeringsinglevalueconverters._1_optional;

import uk.co.jpawlak.maptoobjectconverter.examples._1_registeringsinglevalueconverters.Gender;

import java.util.Optional;

public class Employee {

    public final Optional<Gender> gender;

    public Employee(Gender gender) {
        this.gender = Optional.ofNullable(gender);
    }

}
