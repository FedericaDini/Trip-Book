package it.unipi.aide.DAOs;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.HashMap;
import java.util.Map;

public class GraphDatabaseDAO {
    private Driver driver = null;
    private Session session = null;

    public GraphDatabaseDAO() {
        try {
            driver = GraphDatabase.driver("bolt://172.16.3.145:7687", AuthTokens.basic("neo4j", "superneo"));
            session = driver.session();
        } catch (Exception e) {
            System.out.println("Connection error");
        }
    }

    public void close() {
        session.close();
        driver.close();
    }

    public HashMap<String, String> findRecommendedPlacesByUser(String username) {
        Map<String, Object> params = new HashMap<>();
        params.put("username", username);
        Result result = null;
        try {
            result = session.run("MATCH (u:user{name:$username})-[*]->(p:place) WITH collect(p) as rev, p.category as c MATCH (u2:user)-[*]->(p2:place) WHERE EXISTS { MATCH (u2)-[:reviewed{polarity: true}]->(p3:place) WHERE NOT u2.name=$username AND p3 in rev } AND NOT p2 in rev AND p2.category IN c RETURN DISTINCT p2.ID, p2.name LIMIT 5", params);
        } catch (Exception e) {
            System.out.println("Error during the retrieval of the suggested places");
        }

        HashMap<String, String> results = new HashMap<>();
        if (result != null) {
            while (result.hasNext()) {
                org.neo4j.driver.Record record = result.next();
                results.put(record.get(0).asString(), record.get(1).asString());
            }
        }
        return results;
    }

    public String findMostActiveUser() {
        String user = null;
        Result result = null;
        try {
            result = session.run("MATCH (u:user)-[:reviewed]->(p:place) WITH u,count(p) as rels WHERE rels > 1 RETURN u.name, rels ORDER BY rels DESCENDING LIMIT 1");
        } catch (Exception e) {
            System.out.println("Error during the retrieval of the most active user");
        }

        if (result != null) {
            if (result.hasNext()) {
                Record record = result.next();
                user = record.get(0) + ": " + record.get(1) + " reviews made";
            }
        }
        return user;
    }

    public String findMostReviewedPlace() {
        String place = null;
        Result result = null;
        try {
            result = session.run("MATCH ()-[:reviewed]->(p:place) WITH p, count(p) as rels WHERE rels > 1 RETURN p.name, rels ORDER BY rels DESCENDING LIMIT 1");
        } catch (Exception e) {
            System.out.println("Error during the retrieval of the most reviewed place");
        }

        if (result != null) {
            if (result.hasNext()) {
                Record record = result.next();
                place = record.get(0)+ ": reviewed " + record.get(1) + " times";
                ;
            }
        }
        return place;
    }


    public void addUserNode(String username) throws RuntimeException {
        Map<String, Object> params = new HashMap<>();
        params.put("username", username);
        session.run("MERGE (n:user {name: $username}) RETURN n", params);
    }

    public void addPlaceNode(String id, String category, String name) throws RuntimeException {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("category", category);
        params.put("name", name);
        session.run("MERGE (n:place {name: $name, category: $category, ID: $id}) RETURN n", params);
    }

    public void addReviewedRelationship(String user, String place, String id, Boolean polarity) throws RuntimeException {
        Map<String, Object> params = new HashMap<>();
        params.put("username", user);
        params.put("placeID", place);
        params.put("polarity", polarity);
        params.put("id", id);
        session.run("MATCH (n:user),(p:place) WHERE n.name=$username and p.ID=$placeID CREATE ((n)-[r:reviewed{polarity: $polarity, ID: $id}]->(p))", params);
    }

    public void deleteUserNode(String user) throws RuntimeException {
        Map<String, Object> params = new HashMap<>();
        params.put("username", user);
        session.run("MATCH (n:user { name:$username  }) DETACH DELETE n", params);
        session.run("MATCH (n) WHERE size((n)--())=0 DELETE (n)");
    }

    public void deletePlaceNode(String place) throws RuntimeException {
        Map<String, Object> params = new HashMap<>();
        params.put("placeID", place);
        session.run("MATCH (p:place { ID: $placeID }) DETACH DELETE p", params);
        session.run("MATCH (n) WHERE size((n)--())=0 DELETE (n)");
    }

    public void deleteReviewedRelationship(String relationship) throws RuntimeException {
        Map<String, Object> params = new HashMap<>();
        params.put("relationship", relationship);
        session.run("MATCH ()-[r]->() WHERE r.ID='$relationship' DETACH DELETE r", params);
        session.run("MATCH (n) WHERE size((n)--())=0 DELETE (n)");
    }
}
