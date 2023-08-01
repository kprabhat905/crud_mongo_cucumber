package model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.bson.types.ObjectId;
import utils.ObjectIdDeserializer;

public class User {

    private ObjectId id;
    private String name;
    private int age;

    public User() {
    }

    public User(String name, int age, ObjectId id) {
        this.name = name;
        this.age = age;
        this.id = id;
    }

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @JsonProperty("_id")
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    public String getId() {
        return id != null ? id.toHexString() : null;
    }

    @JsonProperty("_id")
    public void setId(ObjectId id) {
        this.id = id;
    }

    @JsonProperty("name") // Add this annotation to map "name" field during deserialization
    public String getName() {
        return name;
    }

    @JsonProperty("name") // Add this annotation to map "name" field during deserialization
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("age") // Add this annotation to map "age" field during deserialization
    public int getAge() {
        return age;
    }

    @JsonProperty("age") // Add this annotation to map "age" field during deserialization
    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
