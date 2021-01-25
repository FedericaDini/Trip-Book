package it.unipi.aide.DAOs.DocumentsDatabaseDAOs;

import com.mongodb.BasicDBObject;
import com.mongodb.QueryOperators;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.ClientSession;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import it.unipi.aide.Beans.Review;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.simple.JSONArray;

import java.text.SimpleDateFormat;
import java.util.*;

public class ReviewDAO {

    //Enter the database and find all the reviews written about a specific place
    public Review findReviewById(MongoDatabase mongoDB, String id) {

        MongoCollection<Document> reviewsColl = mongoDB.getCollection("reviews");

        Review r = new Review();

        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("_id", new ObjectId(id));
        MongoCursor<Document> cursor = reviewsColl.find(whereQuery).iterator();
        if (cursor.hasNext()) {
            Document d = cursor.next();
            r = new Review(id, d.getDate("date"), d.getDouble("rate"), d.getBoolean("polarity"), d.getString("text"), d.getString("title"), d.getString("user"), d.getString("place_id"), d.getString("place_name"), d.getString("place_category"));
        }
        return r;
    }

    //Enter the database and find all the reviews written by a specific user
    public HashMap<String, String> findReviewsByUsername(MongoDatabase mongoDB, String username) {

        LinkedHashMap<String, String> reviewsList = new LinkedHashMap<>();
        MongoCollection<Document> reviewsColl = mongoDB.getCollection("reviews");

        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("user", username);
        for (Document d : reviewsColl.find(whereQuery).sort(new BasicDBObject("date", -1))) {
            String id = d.getObjectId("_id").toString();

            String pattern = "dd/MM/yyyy HH:mm:ss";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(d.getDate("date"));

            String preview = date + " - " + d.getString("place_name") + " - " + d.getString("title");
            reviewsList.put(id, preview);
        }
        return reviewsList;
    }

    //Enter the database and add a review
    public String addReview(ClientSession clientSession, MongoDatabase database, Review review) {

        String newID = null;

        MongoCollection<Document> reviewsColl = database.getCollection("reviews");

        Document r = new Document()
                .append("date", review.getDate())
                .append("rate", review.getRate())
                .append("polarity", review.isPolarity())
                .append("text", review.getText())
                .append("title", review.getTitle())
                .append("user", review.getUser())
                .append("place_id", review.getPlace_id())
                .append("place_name", review.getPlace_name())
                .append("place_category", review.getPlace_category());

        reviewsColl.insertOne(clientSession, r);
        System.out.println("Place correctly added to MongoDB" + "\n");


        Iterator<Document> i = reviewsColl.find().sort(new Document("_id", -1)).limit(1).iterator();
        if (i.hasNext()) {
            Document d = i.next();
            newID = d.getObjectId("_id").toString();
        }
        return newID;
    }

    //Enter the database and delete all the reviews written by a specific user
    public void deleteReviewsByUsername(ClientSession clientSession, MongoDatabase database, String username) {
        HashMap<String, String> reviews = findReviewsByUsername(database, username);
        for (String key : reviews.keySet()) {
            Review r = findReviewById(database, key);
            deleteReview(clientSession, database, key);
            updateRecentReviewsOfAPlace(clientSession, database, r.getPlace_id());
        }
        System.out.println("All the reviews written by the username have been correctly removed from MongoDB" + "\n");
    }

    //Method to update the 10 most recent reviews of a place
    public void updateRecentReviewsOfAPlace(ClientSession clientSession, MongoDatabase mongoDB, String place) {

        MongoCollection<Document> placesColl = mongoDB.getCollection("places");

        BasicDBObject searchQuery = new BasicDBObject("_id", place);
        BasicDBObject update = new BasicDBObject();
        update.put("$unset", new BasicDBObject("recentReviews", ""));
        update.put("$unset", new BasicDBObject("recentRate", ""));
        placesColl.updateOne(clientSession, searchQuery, update);

        ArrayList<Review> recentReviews = findRecentReviewsOfAPlace(clientSession, mongoDB, place);

        JSONArray jsonList = buildJsonFromReviewsArray(recentReviews);

        BasicDBObject updateFields = new BasicDBObject();
        updateFields.append("recentReviews", jsonList);
        updateFields.append("recentRate", computeMeanRecentRates(recentReviews));
        BasicDBObject setQuery = new BasicDBObject();
        setQuery.append("$set", updateFields);
        placesColl.updateOne(clientSession, searchQuery, setQuery);
    }

    //Method to find the 10 most recent reviews for a place
    public ArrayList<Review> findRecentReviewsOfAPlace(ClientSession clientSession, MongoDatabase database, String place) {

        MongoCollection<Document> reviewsColl = database.getCollection("reviews");

        ArrayList<Review> result = new ArrayList<>();

        for (Document d : reviewsColl.aggregate(clientSession,
                Arrays.asList(
                        Aggregates.match(Filters.eq("place_id", place)),
                        Aggregates.sort(Sorts.descending("date")),
                        Aggregates.limit(10)
                )
        )) {
            Review r = new Review(d.getObjectId("_id").toString(), d.getDate("date"), d.getDouble("rate"), d.getBoolean("polarity"), d.getString("text"), d.getString("title"), d.getString("user"), d.getString("place_id"), d.getString("place_name"), d.getString("place_category"));
            result.add(r);
        }
        return result;
    }

    //Method to create a JSONArray of recent reviews
    private JSONArray buildJsonFromReviewsArray(ArrayList<Review> arrayList) {

        JSONArray jsonArray = new JSONArray();

        for (Review r : arrayList) {
            //Create a review document
            Document review = new Document("_id", new ObjectId(r.getId()))
                    .append("date", r.getDate())
                    .append("rate", r.getRate())
                    .append("polarity", r.isPolarity())
                    .append("text", r.getText())
                    .append("title", r.getTitle())
                    .append("user", r.getUser());

            jsonArray.add(review);
        }

        return jsonArray;
    }

    //Method to compute the mean of the recent rates of a place
    private double computeMeanRecentRates(ArrayList<Review> recentReviews) {
        double sum = 0.0;
        if (!recentReviews.isEmpty()) {
            for (Review r : recentReviews) {
                sum += r.getRate();
            }
            return Math.round(sum / recentReviews.size() * 100.0) / 100.0;
        }
        return sum;
    }

    //Enter the database and delete all the reviews of a place
    public void deleteReviewsByPlaceID(ClientSession clientSession, MongoDatabase database, String place) {
        MongoCollection<Document> reviewColl = database.getCollection("reviews");

        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("place_id", place);
        reviewColl.deleteMany(clientSession, whereQuery);

        System.out.println("The reviews linked to the place have been correctly removed from MongoDB" + "\n");
    }

    //Enter the database and delete a specific review
    public void deleteReview(ClientSession clientSession, MongoDatabase database, String id) {
        MongoCollection<Document> reviewColl = database.getCollection("reviews");

        BasicDBObject whereQuery = new BasicDBObject();
        whereQuery.put("_id", new ObjectId(id));
        reviewColl.deleteOne(clientSession, whereQuery);

        System.out.println("The review has been correctly removed from MongoDB" + "\n");
    }

    public LinkedHashMap<String, String> findTopTenPlaces(MongoDatabase database) {

        MongoCollection<Document> reviewsColl = database.getCollection("reviews");

        LinkedHashMap<String, String> result = new LinkedHashMap<>();

        Bson group = Aggregates.group("$place_id", Accumulators.first("name", "$place_name"), Accumulators.avg("rate", "$rate"));
        Bson sort = Aggregates.sort(Sorts.descending("rate"));
        Bson limit = Aggregates.limit(10);

        for (Document d : reviewsColl.aggregate(Arrays.asList(group, sort, limit))) {
            result.put(d.getString("_id"), d.getString("name") + " - Mean rate: " + d.getDouble("rate").toString());
        }
        return result;
    }

    public LinkedHashMap<String, String> findMeanRatePerCategory(MongoDatabase database) {

        MongoCollection<Document> reviewsColl = database.getCollection("reviews");

        LinkedHashMap<String, String> result = new LinkedHashMap<>();

        Bson group = Aggregates.group("$place_category", Accumulators.avg("rate", "$rate"));
        Bson sort = Aggregates.sort(Sorts.descending("rate"));

        for (Document d : reviewsColl.aggregate(Arrays.asList(group, sort))) {
            result.put(d.getString("_id"), d.getString("_id") + " - Mean rate: " + Math.round(d.getDouble("rate") * 100.0) / 100.0);
        }
        return result;
    }

    public LinkedHashMap<String, String> findHistoryOfCategories(MongoDatabase database) {

        MongoCollection<Document> reviewsColl = database.getCollection("reviews");

        LinkedHashMap<String, String> result = new LinkedHashMap<>();

        Document year = new Document("$year", "$date");

        Map<String, Object> multiIdMap = new HashMap<>();
        multiIdMap.put("year", year);
        multiIdMap.put("category", "$place_category");

        Document groupFields = new Document(multiIdMap);

        Bson group = Aggregates.group(groupFields, Accumulators.avg("rate", "$rate"));
        Bson sort = Aggregates.sort(Sorts.descending("_id"));

        for (Document d : reviewsColl.aggregate(Arrays.asList(group, sort))) {
            Document d1 = (Document) d.get("_id");
            result.put(d1.getInteger("year") + d1.getString("category"), d1.getInteger("year") + " - " + d1.getString("category") + " Mean rate: " + Math.round(d.getDouble("rate") * 100.0) / 100.0);
        }
        return result;
    }
}
