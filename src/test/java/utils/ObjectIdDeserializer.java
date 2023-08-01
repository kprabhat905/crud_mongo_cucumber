package utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;

import java.io.IOException;

public class ObjectIdDeserializer<T> extends JsonDeserializer<T> {

    private Class<T> clazz;

    public ObjectIdDeserializer() {
        // No-arg constructor
    }

    public ObjectIdDeserializer(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        ObjectId id = new ObjectId(node.get("$oid").asText());

        // Get the ObjectMapper from the deserializationContext
        ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();

        // Convert the JsonNode back to a JSON string and deserialize it using ObjectMapper
        String json = node.toString();
        T instance = mapper.readValue(json, clazz);

        setField(instance, "id", id); // Set the id field using reflection
        return instance;
    }

    // Helper method to set a field value using reflection
    private void setField(T instance, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
