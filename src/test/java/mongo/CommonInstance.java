package mongo;

import java.util.HashMap;
import java.util.Map;

public class CommonInstance<T> {
    private static final Map<Class<?>, MongoDBHelper<?>> mongoDBHelpers = new HashMap<>();

    private MongoDBHelper<T> mongoDBHelper;

    // Private constructor to prevent instantiation from outside the class
    private CommonInstance(String collectionName, Class<T> clazz) {
        mongoDBHelper = new MongoDBHelper<>(collectionName, clazz);
    }

    // Factory method to create and retrieve the MongoDBHelper instance
    public static <T> MongoDBHelper<T> getMongoDBHelper(String collectionName, Class<T> clazz) {
        return new CommonInstance<>(collectionName, clazz).mongoDBHelper;
    }

    // Method to close the MongoDBHelper instance
    public static <T> void closeMongoDBHelper(MongoDBHelper<T> mongoDBHelper) {
        if (mongoDBHelper != null) {
            mongoDBHelper.close();
        }
    }
}
