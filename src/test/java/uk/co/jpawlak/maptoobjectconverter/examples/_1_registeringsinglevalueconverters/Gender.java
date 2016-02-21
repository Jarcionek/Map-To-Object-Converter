package uk.co.jpawlak.maptoobjectconverter.examples._1_registeringsinglevalueconverters;

import static java.util.Arrays.stream;

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
