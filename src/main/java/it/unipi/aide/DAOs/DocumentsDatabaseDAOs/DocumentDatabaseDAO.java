package it.unipi.aide.DAOs.DocumentsDatabaseDAOs;

import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.client.*;
import com.mongodb.client.model.Indexes;
import org.bson.Document;

public class DocumentDatabaseDAO {

    private MongoClient client = null;
    private MongoDatabase database = null;

    public MongoClient getClient() {
        return client;
    }

    public void setClient(MongoClient client) {
        this.client = client;
    }

    public MongoDatabase getMongoDatabase() {
        return database;
    }

    public void openConnection(String uri) {

        // Creating a Mongo client
        ConnectionString connectionString = new ConnectionString(uri);
        client = MongoClients.create(connectionString);

        // Accessing the database
        database = client.getDatabase("trip-book");

        //Creating the indexes if not exist
        createIndexes();
    }

    public boolean isNewID(MongoCollection<Document> collection, String id) {
        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("_id", id);
        MongoCursor<Document> cursor = collection.find(whereQuery).iterator();
        return !cursor.hasNext();
    }

    public void createIndexes() {

        //To speed up the place search by key words
        database.getCollection("places").createIndex(Indexes.text("name"));

        //To speed up the computation when searching a user by username
        database.getCollection("users").createIndex(Indexes.ascending("username"));

        //To speed up the computation when searching the reviews of a user
        database.getCollection("reviews").createIndex(Indexes.ascending("user"));
    }

    public void closeConnection() {
        client.close();
    }
}
