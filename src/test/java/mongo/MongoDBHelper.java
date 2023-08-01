package mongo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import model.User;
import org.bson.Document;
import org.bson.types.ObjectId;
import com.fasterxml.jackson.databind.ObjectMapper;
import utils.ObjectIdDeserializer;
import utils.UserDeserializer;

import java.io.IOException;

public class MongoDBHelper<T> {

    private final String collectionName;
    private final ObjectMapper objectMapper;
    private final Class<T> clazz;
    private MongoCollection<Document> collection;

    public MongoDBHelper(String collectionName, Class<T> clazz) {
        this.collectionName = collectionName;
        this.clazz = clazz;
        this.collection = MongoClients.create("mongodb://localhost:27017").getDatabase("my_database").getCollection(collectionName);

        this.objectMapper = new ObjectMapper();
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        // Register custom deserializers for User and ObjectId
        SimpleModule module = new SimpleModule();
        module.addDeserializer(User.class, new UserDeserializer());
        module.addDeserializer(ObjectId.class, new ObjectIdDeserializer<>());
        this.objectMapper.registerModule(module);
    }

    public String insertDocument(T object) {
        String json = serializeToJson(object);
        Document document = Document.parse(json);
        collection.insertOne(document);

        // Return the generated ObjectId
        ObjectId generatedId = document.getObjectId("_id");
        return generatedId.toString();
    }

    public T getDocumentById(String id) {
        Document document = collection.find(Filters.eq("_id", new ObjectId(id))).first();
        if (document != null) {
            try {
                T x = objectMapper.readValue(document.toJson(), clazz);
                return x;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public void updateDocument(String id, T updatedObject) {
        String json = serializeToJson(updatedObject);
        Document updatedDocument = Document.parse(json);

        // Exclude the _id field from the update operation
        updatedDocument.remove("_id");

        collection.replaceOne(new Document("_id", new ObjectId(id)), updatedDocument);
    }


    public void deleteDocument(String id) {
        collection.deleteOne(new Document("_id", new ObjectId(id)));
    }

    private String serializeToJson(T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private T deserializeFromJson(String jsonString, Class<T> objectType) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(jsonString, objectType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Custom serializer for ObjectId
    private static class ObjectIdSerializer extends com.fasterxml.jackson.databind.ser.std.StdSerializer<ObjectId> {
        public ObjectIdSerializer() {
            super(ObjectId.class);
        }

        @Override
        public void serialize(ObjectId value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.toHexString());
        }
    }
}
