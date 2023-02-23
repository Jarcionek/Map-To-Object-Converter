package uk.co.jpawlak.maptoobjectconverter;

import com.google.gson.Gson;
import org.junit.ComparisonFailure;

public class TestUtil {

    private final static Gson GSON = new Gson();

    public static <T> void assertObjectsEqual(T actual, T expected) {
        String actualJson = GSON.toJson(actual);
        String expectedJson = GSON.toJson(actual);

        if (!expectedJson.equals(actualJson)) {
            throw new ComparisonFailure(null, expectedJson, actualJson);
        }
    }

}
