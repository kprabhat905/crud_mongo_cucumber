package model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import utils.ObjectIdDeserializer;

import java.io.IOException;

public class MongoDBHelper<T> implements AutoCloseable {

    private static final String sshHost = "";
    private static final String sshUser = "";
    private static final int sshPort = 22;
    private static final String privateKeyPath = ""; // Update with your private key path
    private static final String dbUser = "";
    private static final String dbPassword = "";
    private static final int dbPort = 27017;
    private static final int CONNECTION_TIMEOUT = 30000;
    private static final String dbHost = "";
    //    public static MongoClient mongoClient;
//    public static MongoDatabase db;
    public static MongoCollection<Document> collection;
    private static Session sshSession;
    private final String collectionName;
    private final String dbName = "";
    private final String authSource = "";
    private final ObjectMapper objectMapper;
    private final Class<T> clazz;
    protected static MongoClient mongoClient;


    public MongoDBHelper(String collectionName, Class<T> clazz, SimpleModule module) {
        connectToSSHServer();
        this.collectionName = collectionName;
        this.clazz = clazz;
        String connectionString = null;
        try {
            connectionString = "mongodb://" + dbUser + ":" + dbPassword + "@" + "localhost" + ":" + sshSession.setPortForwardingL(dbPort, dbHost, dbPort) + "/" + dbName + "?authMechanism=SCRAM-SHA-256&authSource=" + authSource;
        } catch (JSchException e) {
            throw new RuntimeException(e);
        }

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .build();

        // Connect to the MongoDB server
        mongoClient = MongoClients.create(settings);
        collection = mongoClient.getDatabase(dbName).getCollection(collectionName);

        this.objectMapper = new ObjectMapper();
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        this.objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        // Register custom deserializers for User and ObjectId
//        SimpleModule module = new SimpleModule();
//        module.addDeserializer(User.class, new UserDeserializer());
        module.addDeserializer(ObjectId.class, new ObjectIdDeserializer<>());
        this.objectMapper.registerModule(module);
    }

    private static void connectToSSHServer() {
        try {
            JSch jsch = new JSch();

            // Load private key for public key authentication
            jsch.addIdentity(privateKeyPath);

            sshSession = jsch.getSession(sshUser, sshHost, sshPort);
            sshSession.setConfig("StrictHostKeyChecking", "no"); // Disable host key checking for simplicity
            sshSession.setConfig("PreferredAuthentications", "publickey");
            sshSession.setConfig("ConnectTimeout", String.valueOf(CONNECTION_TIMEOUT));

            sshSession.connect();
            if (sshSession.isConnected()) {
                System.out.println("SSH Connection successful");
            } else {
                System.out.println("SSH Connection failed!");
            }

        } catch (JSchException e) {
            e.printStackTrace();
        }
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
                return objectMapper.readValue(document.toJson(), clazz);
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

    @Override
    public void close() throws Exception {
        try {
            if (mongoClient != null) {
                mongoClient.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (sshSession != null) {
                sshSession.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
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
