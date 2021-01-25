package it.unipi.aide.DAOs.DocumentsDatabaseDAOs;

import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import it.unipi.aide.Beans.Place;
import it.unipi.aide.Beans.Review;
import org.bson.Document;

import java.util.*;

public class PlaceDAO {

    //Enter the database and find all the places that contains a specific search string
    public HashMap<String, String> findPlacesByString(MongoDatabase database, String string) {

        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        MongoCollection<Document> placesColl = database.getCollection("places");

        //Query using an index provided by MongoDB
        FindIterable<Document> cursor = placesColl.find(new BasicDBObject("$text", new BasicDBObject("$search", string)));

        for (Document d : cursor) {
            map.put(d.getString("_id"), d.getString("name"));
        }

        return map;
    }

    //Enter the database and find a specific place
    public Place findPlaceById(MongoDatabase database, String id) {

        Place p = null;

        MongoCollection<Document> placesColl = database.getCollection("places");

        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("_id", id);
        MongoCursor<Document> cursor = placesColl.find(whereQuery).iterator();
        if (cursor.hasNext()) {
            Document d = cursor.next();

            ArrayList<String> categoriesList = new ArrayList<>();

            List<String> categories = (List<String>) d.get("categories");
            if (categories != null) {
                categoriesList.addAll(categories);
            }

            ArrayList<Review> recentReviews = new ArrayList<>();
            ArrayList<Document> jsonReviews = (ArrayList<Document>) d.get("recentReviews");
            for (Document d1 : jsonReviews) {
                Review r = new Review(d1.getObjectId("_id").toString(), d1.getDate("date"), d1.getDouble("rate"), d1.getBoolean("polarity"), d1.getString("text"), d1.getString("title"), d1.getString("user"), d.getString("_id"), d.getString("name"), d.getString("mainCategory"));
                recentReviews.add(r);
            }

            p = new Place(d.getString("_id"), d.getString("name"), d.getString("address"), d.getString("location"), d.getString("mainCategory"), categoriesList, d.getString("latitude"), d.getString("longitude"), d.getString("telephone"), d.getString("website"), d.getDouble("recentRate"), recentReviews);
        }

        return p;
    }

    public void addPlace(MongoDatabase database, Place place) {

        MongoCollection<Document> placesColl = database.getCollection("places");

        Document p = new Document("_id", place.getId())
                .append("name", place.getName())
                .append("address", place.getAddress())
                .append("categories", place.getCategories())
                .append("mainCategory", place.getMain_category())
                .append("location", place.getLocation())
                .append("latitude", place.getLatitude())
                .append("longitude", place.getLongitude())
                .append("recentRate", place.getRecentRate())
                .append("recentReviews", place.getRecentReviews())
                .append("website", place.getWebsite())
                .append("telephone", place.getTelephone());

        placesColl.insertOne(p);
        System.out.println("Place correctly added to MongoDB" + "\n");
    }


    public void deletePlaceByID(ClientSession clientSession, MongoDatabase database, String id) {

        MongoCollection<Document> placesColl = database.getCollection("places");

        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("_id", id);
        placesColl.deleteOne(clientSession, whereQuery);
        System.out.println("Place correctly removed from MongoDB" + "\n");
    }
}
