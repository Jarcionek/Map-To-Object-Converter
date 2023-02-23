package uk.co.jpawlak.maptoobjectconverter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.junit.ComparisonFailure;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

public class TestUtil {

    private final static Gson GSON;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(OptionalTypeAdapter.FACTORY);
        GSON = builder.create();
    }

    public static class OptionalTypeAdapter<E> extends TypeAdapter<Optional<E>> {

        public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
            @SuppressWarnings({"unchecked", "rawtypes"})
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                Class<T> rawType = (Class<T>) type.getRawType();
                if (rawType != Optional.class) {
                    return null;
                }
                final ParameterizedType parameterizedType = (ParameterizedType) type.getType();
                final Type actualType = parameterizedType.getActualTypeArguments()[0];
                final TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(actualType));
                return new OptionalTypeAdapter(adapter);
            }
        };

        private final TypeAdapter<E> adapter;

        public OptionalTypeAdapter(TypeAdapter<E> adapter) {
            this.adapter = adapter;
        }

        @Override
        public void write(JsonWriter out, Optional<E> value) throws IOException {
            if (value.isPresent()) {
                adapter.write(out, value.get());
            } else {
                out.nullValue();
            }
        }

        @Override
        public Optional<E> read(JsonReader in) throws IOException {
            JsonToken peek = in.peek();
            if (peek != JsonToken.NULL) {
                return Optional.ofNullable(adapter.read(in));
            }

            in.nextNull();
            return Optional.empty();
        }

    }

    public static <T> void assertObjectsEqual(T actual, T expected) {
        String actualJson = GSON.toJson(actual);
        String expectedJson = GSON.toJson(actual);

        if (!expectedJson.equals(actualJson)) {
            throw new ComparisonFailure(null, expectedJson, actualJson);
        }
    }

}
