# crud_mongo_cucumber

To set up MongoDB locally on a Mac and create a database, follow these steps:

1. Install MongoDB:

- The recommended way to install MongoDB on a Mac is using Homebrew. If you don't have Homebrew installed, you can do so by following the instructions on the Homebrew website (https://brew.sh/).
- Once you have Homebrew, open a terminal and run the following command to install MongoDB:
- brew tap mongodb/brew
- brew install mongodb-community

2. Start the MongoDB Server:

- By default, Homebrew will start the MongoDB server as a background service after installation. If it's not started automatically, you can start it using the following command:
- brew services start mongodb/brew/mongodb-community

3. Connect to MongoDB:

- Open a terminal and run the mongo command. This will open the MongoDB shell and connect to the default local server (running on port 27017).

4. Create a Database:

- In the MongoDB shell, to create a new database, use the use command followed by the database name you want to create. For example:
- use mydatabase
- Note that the database will not be actually created until you insert data into it.

That's it! You have now set up MongoDB locally on your Mac and created a database. You can start inserting data and working with collections within this database. Remember to secure your database properly if you plan to use it in a production environment.

To stop the MongoDB service, you can use the following command:
- brew services stop mongodb/brew/mongodb-community


