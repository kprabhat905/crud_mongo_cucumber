Feature: CRUD Operations with MongoDB

  Scenario: Insert a new User document
    Given I have a new User with name "John" and age "30"
    When I insert the User document
    Then the User document is successfully inserted

  Scenario: Retrieve a User document by ID
    #Given I have a User with ID "64c9517073256d735e38a928"
    When I retrieve the User document by ID
    Then the retrieved User document should have name "John" and age "30"

  Scenario: Update a User document by ID
    #Given I have a User with ID "64c9517073256d735e38a928"
    When I update the User document with name "John" and age "35"
    Then the updated User document should have name "John" and age "35"

  Scenario: Delete a User document by ID
    #Given I have a User with ID "64c9517073256d735e38a928"
    When I delete the User document
    Then the User document is successfully deleted
