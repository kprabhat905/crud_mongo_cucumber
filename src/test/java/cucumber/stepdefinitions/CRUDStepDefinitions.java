package cucumber.stepdefinitions;

import com.fasterxml.jackson.core.type.TypeReference;
import io.cucumber.java.en.*;
import model.User;
import mongo.MongoDBHelper;
import org.bson.types.ObjectId;

import static org.junit.Assert.*;

public class CRUDStepDefinitions {
    private static MongoDBHelper<User> mongoDBHelper;
    private static User user;
    private static String userId;
    private static User insertedUser;
    private static User retrievedUser;

    @Given("I have a new User with name {string} and age {string}")
    public void i_have_a_new_user_with_name_and_age(String name, String age) {
        user = new User(name, Integer.parseInt(age));
    }

    @When("I insert the User document")
    public void i_insert_the_user_document() {
        mongoDBHelper = new MongoDBHelper<>("users", User.class);
        String generatedId = mongoDBHelper.insertDocument(user);
        user.setId(new ObjectId(generatedId)); // Convert string to ObjectId and set
        insertedUser = user;
    }

    @Then("the User document is successfully inserted")
    public void the_user_document_is_successfully_inserted() {
        assertNotNull(user.getId());
        userId = user.getId().toString();
    }

    @When("I retrieve the User document by ID")
    public void iRetrieveTheUserDocumentByID() {
        // Create a new instance of MongoDBHelper for User
        MongoDBHelper<User> userMongoDBHelper = new MongoDBHelper<>("users", User.class);

        // Retrieve the User document using the updated MongoDBHelper
        retrievedUser = userMongoDBHelper.getDocumentById(userId);
    }

    @Then("the retrieved User document should have name {string} and age {string}")
    public void theRetrievedUserDocumentShouldHaveNameAndAge(String name, String age) {
        assertNotNull(retrievedUser);
        assertEquals(name, retrievedUser.getName());
        assertEquals(Integer.parseInt(age), retrievedUser.getAge());
    }

    @When("I update the User document with name {string} and age {string}")
    public void iUpdateTheUserDocumentWithNameAndAge(String name, String age) {
        user.setName(name);
        user.setAge(Integer.parseInt(age));
        mongoDBHelper.updateDocument(user.getId().toString(), user);
    }

    @Then("the updated User document should have name {string} and age {string}")
    public void theUpdatedUserDocumentShouldHaveNameAndAge(String name, String age) {
        user = mongoDBHelper.getDocumentById(userId);
        assertNotNull(user);
        assertEquals(name, user.getName());
        assertEquals(Integer.parseInt(age), user.getAge());
    }

    @When("I delete the User document")
    public void iDeleteTheUserDocument() {
        mongoDBHelper.deleteDocument(user.getId().toString());
    }

    @Then("the User document is successfully deleted")
    public void theUserDocumentIsSuccessfullyDeleted() {
        user = mongoDBHelper.getDocumentById(userId);
        assertNull(user);
    }

    @Given("I have a User with ID {string}")
    public void i_have_a_User_with_ID(String userId) {
        mongoDBHelper = new MongoDBHelper<>("users", User.class);
        this.userId = userId;
        user = mongoDBHelper.getDocumentById(userId);
        assertNotNull(user);
    }
}
