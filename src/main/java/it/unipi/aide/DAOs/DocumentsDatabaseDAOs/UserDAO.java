package it.unipi.aide.DAOs.DocumentsDatabaseDAOs;

import com.mongodb.BasicDBObject;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import it.unipi.aide.Beans.User;
import org.bson.Document;

public class UserDAO {

    //Enter the database and find the user with a specific username
    public User findUserByUsername(MongoDatabase database, String username) {

        MongoCollection<Document> usersColl = database.getCollection("users");

        User u = null;

        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("username", username);
        MongoCursor<Document> cursor = usersColl.find(whereQuery).iterator();
        if (cursor.hasNext()) {
            Document d = cursor.next();
            u = new User(username, d.getString("password"), d.getString("city"), d.getString("province"), d.getString("type"));
        }
        return u;
    }

    //Enter the database and check if the given username exists or not
    public boolean existingUser(MongoDatabase database, String username) {
        User user = findUserByUsername(database, username);
        return user != null;
    }

    //Enter the database and add a new user
    public void addUser(MongoDatabase database, User user) {

        MongoCollection<Document> usersColl = database.getCollection("users");

        Document u = new Document("username", user.getUsername())
                .append("password", user.getPassword())
                .append("city", user.getCity())
                .append("province", user.getProvince())
                .append("type", user.getType());

        usersColl.insertOne(u);
        System.out.println("User correctly added to MongoDB" + "\n");
    }

    //Delete the user from the database
    public void deleteUser(ClientSession clientSession, MongoDatabase database, String username) {
        MongoCollection<Document> usersColl = database.getCollection("users");
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("username", username);
        usersColl.deleteOne(whereQuery);
        System.out.println("User correctly removed from MongoDB" + "\n");
    }
}
