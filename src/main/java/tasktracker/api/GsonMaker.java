package tasktracker.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class GsonMaker {
    public Gson makeSpesialGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {
            @Override
            public void write(JsonWriter out, LocalDateTime value) throws IOException {
                out.value(String.valueOf(value));
            }

            @Override
            public LocalDateTime read(JsonReader in) throws IOException {
                try {
                    return LocalDateTime.parse(in.nextString());
                } catch (DateTimeParseException exception) {
                    return null;
                }
            }
        });
        return gsonBuilder.create();
    }
}
