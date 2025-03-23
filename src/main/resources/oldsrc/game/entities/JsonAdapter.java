package game.entities;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.List;

class JsonAdapter implements JsonSerializer<List<?>> {

    @Override
    public JsonElement serialize(List<?> src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null || src.isEmpty()) // exclusion is made here
            return null;

        JsonArray array = new JsonArray();

        for (Object child : src) {
            JsonElement element = context.serialize(child);
            array.add(element);
        }

        return array;
    }
}

