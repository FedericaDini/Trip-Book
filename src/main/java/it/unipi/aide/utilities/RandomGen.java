package it.unipi.aide.utilities;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import org.bson.Document;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomGen {
    public static String generateRandomString(int length) {
        // Possible characters inside the password (0-9, a-z, A-Z)
        final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        SecureRandom secureRandom = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder();

        // Choose a character randomly and append it to the new password
        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            stringBuilder.append(characters.charAt(randomIndex));
        }

        return stringBuilder.toString();
    }

    public static Date generateRandomDate() {
        Date d1 = new Date("January 1, 2010, 00:00:00 GMT");
        Date d2 = new Date("December 31, 2020, 00:00:00 GMT");
        return new Date(ThreadLocalRandom.current().nextLong(d1.getTime(), d2.getTime()));
    }

    public static String returnRandomUsername(MongoDatabase database) {
        MongoCollection<Document> usersColl = database.getCollection("users");
        AggregateIterable<Document> iterable = usersColl.aggregate(Arrays.asList(Aggregates.sample(1)));
        Document d = iterable.first();
        assert d != null;
        return d.getString("username");
    }

    public static double generateRandomRate() {
        int n = new Random().nextInt(6);
        return Double.parseDouble(String.valueOf(n));
    }
}
