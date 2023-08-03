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
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.mongodb.client.MongoClient;
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
    private static final String sshHost = "";
    private static final String sshUser = "";
    private static final int sshPort = 22;
    private static final String privateKeyPath = ""; // Update with your private key path
    private final String authSource = "";
    private final String authMechanism = "";
    private final String dbName = "";
    private static final String dbUser = "";
    private static final String dbPassword = "";
    private static final int dbPort = 27017;
    private static final int CONNECTION_TIMEOUT = 30000;
    private static final String dbHost = "";

    private static Session sshSession;

    private final String collectionName;
    private final ObjectMapper objectMapper;
    private final Class<T> clazz;
    private MongoCollection<Document> collection;
    private final MongoClient mongoClient;

    // Private static instance variable
    private static MongoDBHelper<?> instance;

    // Public static method to get the singleton instance
    public static synchronized <T> MongoDBHelper<T> getInstance(String collectionName, Class<T> clazz) {
        if (instance == null) {
            instance = new MongoDBHelper<>(collectionName, clazz);
        }
        return (MongoDBHelper<T>) instance;
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

    public MongoDBHelper(String collectionName, Class<T> clazz) {
        //connectToSSHServer(); if SSH connection is required
        this.collectionName = collectionName;
        this.clazz = clazz;

        try {
            String connectionStringServer = "mongodb://" + dbUser + ":" + dbPassword + "@" + "localhost" + ":" + sshSession.setPortForwardingL(dbPort, dbHost, dbPort) + "/" + dbName + "?authMechanism="+authMechanism+"&authSource=" + authSource;

        } catch (JSchException e) {
            throw new RuntimeException(e);
        }
        String connectionString = "mongodb://localhost:27017";
        this.mongoClient = MongoClients.create(connectionString);
        this.collection = mongoClient.getDatabase("my_database").getCollection(collectionName);


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

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

}
