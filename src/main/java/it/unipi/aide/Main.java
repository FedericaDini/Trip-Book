package it.unipi.aide;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.TransactionBody;

import it.unipi.aide.Beans.*;
import it.unipi.aide.DAOs.DocumentsDatabaseDAOs.*;
import it.unipi.aide.DAOs.GraphDatabaseDAO;
import it.unipi.aide.utilities.ListsManagement;
import it.unipi.aide.utilities.Validation;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Main {

    private BufferedReader inKeyboard;
    private PrintWriter outVideo;

    private final DocumentDatabaseDAO connection;
    private final PlaceDAO placeDAO = new PlaceDAO();
    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final UserDAO userDAO = new UserDAO();

    private final MongoDatabase docDatabase;

    User user = null;

    public Main() {


        //Setting of the variables for IO operations
        prepareIO();

        //Open connection to MongoDB
        connection = new DocumentDatabaseDAO();
        connection.openConnection("mongodb://172.16.3.145:27020,172.16.3.146:27020,172.16.3.102:27020/" + "retryWrites=true&w=2&wtimeout=10000&readPreference=primary");

        docDatabase = connection.getMongoDatabase();

        outVideo.println("Welcome to Trip-Book!");

        //Real execution of the application
        execute();
    }

    private void prepareIO() {
        inKeyboard = new BufferedReader(new InputStreamReader(System.in));
        outVideo = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)), true);
    }

    private void execute() {

        while (true) {
            if (user != null) {
                outVideo.println();
                switch (user.getType()) {
                    case "NORMAL":
                        normalExecution();
                        break;
                    case "COORD":
                        coordExecution();
                        break;
                    case "ADMIN":
                        adminExecution();
                        break;
                }

                //Logout
                user = null;

            } else {
                outVideo.println("Press 'a' to authenticate, 'q' to quit");
                String choice = null;
                while (choice == null) {
                    try {
                        choice = inKeyboard.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    assert choice != null;
                    if (choice.equalsIgnoreCase("a")) {
                        user = authenticate();
                        outVideo.println("Hello, " + user.getUsername());
                    } else if (choice.equalsIgnoreCase("q")) {
                        outVideo.println("Goodbye!");
                        //Close connection to document database
                        connection.closeConnection();
                        return;
                    } else {
                        outVideo.println("INVALID INPUT");
                    }
                }
            }
        }
    }

    private User authenticate() {
        outVideo.println("Press 'l' to login, 'r' to register");

        User u = null;

        String action = null;
        while (action == null) {
            try {
                action = inKeyboard.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            assert action != null;
            if (action.equalsIgnoreCase("l")) {
                u = login();
            } else if (action.equalsIgnoreCase("r")) {
                //With the registration only NORMAL USERS can be created
                u = register("NORMAL");
            } else {
                action = null;
                outVideo.println("INVALID INPUT");
            }
        }

        return u;
    }

    public User login() {

        User u = null;
        boolean logged = false;

        while (!logged) {
            outVideo.println("Username:");
            try {
                String username = inKeyboard.readLine();

                outVideo.println("Password:");

                String password = inKeyboard.readLine();

                u = userDAO.findUserByUsername(docDatabase, username);

                if (u != null && password.equals(u.getPassword())) {
                    logged = true;
                } else {
                    outVideo.println("Wrong credentials, try again!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return u;
    }

    private User register(String type) {

        User u = null;

        try {

            //Insert a new username
            String username = null;
            while (username == null) {
                outVideo.println("Insert username:");
                username = inKeyboard.readLine();

                if (userDAO.existingUser(docDatabase, username)) {
                    username = null;
                    outVideo.println("Username already in use");
                }
            }

            //Insert a valid password
            String password = null;
            while (password == null) {
                outVideo.println("Insert a password with at least 8 alphanumeric characters:");
                password = inKeyboard.readLine();

                if (!Validation.validatePassword(password, 8)) {
                    password = null;
                }
            }

            //Insert city and province
            outVideo.println("Insert your city:");
            String city = inKeyboard.readLine();


            outVideo.println("Insert your province:");
            String province = inKeyboard.readLine();


            u = new User(username, password, city, province, type);
            userDAO.addUser(docDatabase, u);

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return u;
    }

    private void normalExecution() {

        //SEE THE SUGGESTED PLACES
        outVideo.println();
        outVideo.println("-----------------------------------------------------------------------------------");
        outVideo.println("These are the suggested places for the user:");
        GraphDatabaseDAO graphDatabaseDAO = new GraphDatabaseDAO();
        HashMap<String, String> suggestedList = graphDatabaseDAO.findRecommendedPlacesByUser(user.getUsername());
        graphDatabaseDAO.close();
        ListsManagement.showResults(suggestedList);

        //BROWSE, FIND AND VIEW A PLACE
        outVideo.println();
        outVideo.println("-----------------------------------------------------------------------------------");
        outVideo.println("SEARCH A PLACE BY SEARCH STRING");
        outVideo.println("Let's suppose we want to find the Louvre's museum typing the word 'louvre'");
        HashMap<String, String> resultsList = placeDAO.findPlacesByString(docDatabase, "louvre");
        outVideo.println("These are the results found:");
        ListsManagement.showResults(resultsList);

        //SEE THE DETAILS OF A PLACE
        outVideo.println();
        outVideo.println("-----------------------------------------------------------------------------------");
        outVideo.println("We are interested in the eighth place, so we select it to see its details:");
        String placeID = ListsManagement.retrieveIdFromSelectedRaw(resultsList, 8);
        Place p = placeDAO.findPlaceById(docDatabase, placeID);
        outVideo.println(p);

        //ADD A REVIEW
        outVideo.println();
        outVideo.println("-----------------------------------------------------------------------------------");
        outVideo.println("We want to add a review about this place, so we insert it into the application");
        GraphDatabaseDAO graphDatabaseDAO2 = new GraphDatabaseDAO();
        ClientSession clientSession = connection.getClient().startSession();
        TransactionBody txnBody = (TransactionBody<String>) () -> {
            Review review = new Review(new Date(), 5, true, "I went there years ago and it was wonderful!", "Very beautiful place!", user.getUsername(), p.getId(), p.getName(), p.getMain_category());
            String id = reviewDAO.addReview(clientSession, docDatabase, review);
            graphDatabaseDAO2.addUserNode(review.getUser());
            graphDatabaseDAO2.addPlaceNode(review.getPlace_id(), review.getPlace_category(), review.getPlace_name());
            graphDatabaseDAO2.addReviewedRelationship(review.getUser(), review.getPlace_id(), id, review.isPolarity());
            reviewDAO.updateRecentReviewsOfAPlace(clientSession, docDatabase, p.getId());
            return "The transaction ended correctly, all the data is consistent";
        };
        try {
            clientSession.withTransaction(txnBody);
        } catch (RuntimeException e) {
            outVideo.println("Something goes wrong with the transaction, please try again");
        } finally {
            clientSession.close();
            graphDatabaseDAO2.close();
        }

        //BROWSE, FIND AND VIEW THE REVIEWS MADE
        outVideo.println();
        outVideo.println("-----------------------------------------------------------------------------------");
        outVideo.println("SEE MY REVIEWS");
        outVideo.println("The application searches for all your reviews");
        HashMap<String, String> reviewsList = reviewDAO.findReviewsByUsername(docDatabase, user.getUsername());
        outVideo.println("These are the reviews written by you:");
        ListsManagement.showResults(reviewsList);

        //SEE THE DETAILS OF A REVIEW
        outVideo.println();
        outVideo.println("-----------------------------------------------------------------------------------");
        outVideo.println("SEE THE DETAILS OF A REVIEW");
        outVideo.println("We are interested in the review we have just wrote, so we select it to see its details:");
        String reviewID = ListsManagement.retrieveIdFromSelectedRaw(reviewsList, 1);
        Review r = reviewDAO.findReviewById(docDatabase, reviewID);
        outVideo.println(r);

        //DELETE A REVIEW
        outVideo.println();
        outVideo.println("-----------------------------------------------------------------------------------");
        outVideo.println("DELETE A REVIEW");
        outVideo.println("We want to remove the review, so we eliminate it from the application");
        GraphDatabaseDAO graphDatabaseDAO3 = new GraphDatabaseDAO();
        ClientSession clientSession2 = connection.getClient().startSession();
        txnBody = (TransactionBody<String>) () -> {
            reviewDAO.deleteReview(clientSession2, docDatabase, r.getId());
            reviewDAO.updateRecentReviewsOfAPlace(clientSession2, docDatabase, r.getPlace_id());
            graphDatabaseDAO3.deleteReviewedRelationship(r.getId());
            return "The transaction ended correctly, all the data is consistent";
        };
        try {
            clientSession2.withTransaction(txnBody);
        } catch (RuntimeException e) {
            outVideo.println("Something goes wrong with the transaction, please try again");
        } finally {
            clientSession2.close();
            graphDatabaseDAO3.close();
        }
    }

    private void coordExecution() {

        //ADD A NEW PLACE
        outVideo.println();
        outVideo.println("-----------------------------------------------------------------------------------");
        outVideo.println("ADD A NEW PLACE");
        outVideo.println("We want to add Central Park to the places, so we insert it into the application");
        ArrayList<String> categories = new ArrayList<>();
        categories.add("green place");
        categories.add("natural park");
        Place place = new Place("abcdef12345", "Central Park zzz", "New York City", "US – NY", "Parks", categories, "40.785091", "-73.968285", "", "https://www.centralparknyc.org/", 0.0, new ArrayList<>());
        placeDAO.addPlace(docDatabase, place);

        //BROWSE, FIND AND VIEW A PLACE
        outVideo.println();
        outVideo.println("-----------------------------------------------------------------------------------");
        outVideo.println("SEARCH A PLACE BY SEARCH STRING");
        outVideo.println("We want to find the place we have already created, so we insert 'zzz'");
        HashMap<String, String> resultsList = placeDAO.findPlacesByString(docDatabase, "zzz");
        outVideo.println("These are the results found:");
        ListsManagement.showResults(resultsList);

        //SEE THE DETAILS OF A PLACE
        outVideo.println();
        outVideo.println("-----------------------------------------------------------------------------------");
        outVideo.println("We are interested in the first place, so we select it to see its details:");
        String placeID = ListsManagement.retrieveIdFromSelectedRaw(resultsList, 1);
        Place p = placeDAO.findPlaceById(docDatabase, placeID);
        outVideo.println(p);

        //DELETE A PLACE
        outVideo.println();
        outVideo.println("-----------------------------------------------------------------------------------");
        outVideo.println("DELETE A PLACE");
        outVideo.println("We want to remove the place, so we eliminate it from the application");
        GraphDatabaseDAO graphDatabaseDAO = new GraphDatabaseDAO();
        ClientSession clientSession = connection.getClient().startSession();
        TransactionBody txnBody = (TransactionBody<String>) () -> {
            graphDatabaseDAO.deletePlaceNode(p.getId());
            placeDAO.deletePlaceByID(clientSession, docDatabase, p.getId());
            reviewDAO.deleteReviewsByPlaceID(clientSession, docDatabase, p.getId());
            return "The transaction ended correctly, all the data is consistent";
        };
        try {
            clientSession.withTransaction(txnBody);
        } catch (RuntimeException e) {
            outVideo.println("Something goes wrong with the transaction, please try again");
        } finally {
            clientSession.close();
            graphDatabaseDAO.close();
        }
    }

    private void adminExecution() {

        //ADD A NEW USER
        outVideo.println();
        outVideo.println("-----------------------------------------------------------------------------------");
        outVideo.println("ADD A NEW USER");
        outVideo.println("We want to add a new user, so we insert it into the application");
        User u = new User("federica", "password", "Arezzo", "AR", "NORMAL");
        userDAO.addUser(docDatabase, u);

        //BROWSE, FIND AND VIEW A USER
        outVideo.println();
        outVideo.println("-----------------------------------------------------------------------------------");
        outVideo.println("SEARCH A USER BY USERNAME");
        outVideo.println("We want to find the user we have already created, so we insert 'federica'");
        User u2 = userDAO.findUserByUsername(docDatabase, "federica");
        outVideo.println("This is the result of the research:");
        outVideo.println(u2);

        //DELETE A USER
        outVideo.println();
        outVideo.println("-----------------------------------------------------------------------------------");
        outVideo.println("DELETE A USER");
        outVideo.println("We want to remove the user, so we eliminate it from the application");
        GraphDatabaseDAO graphDatabaseDAO = new GraphDatabaseDAO();
        ClientSession clientSession = connection.getClient().startSession();
        TransactionBody txnBody = (TransactionBody<String>) () -> {
            graphDatabaseDAO.deleteUserNode(user.getUsername());
            userDAO.deleteUser(clientSession, docDatabase, u2.getUsername());
            reviewDAO.deleteReviewsByUsername(clientSession, docDatabase, u2.getUsername());
            return "The transaction ended correctly, all the data is consistent";
        };
        try {
            clientSession.withTransaction(txnBody);
        } catch (RuntimeException e) {
            outVideo.println("Something goes wrong with the transaction, please try again");
        } finally {
            clientSession.close();
            graphDatabaseDAO.close();
        }

        //SEE STATISTICS
        outVideo.println();
        outVideo.println("-----------------------------------------------------------------------------------");
        outVideo.println("SEE STATISTICS ABOUT THE APPLICATION");
        outVideo.println("These are the statistics that the system computes:");

        outVideo.println("-----------------------------------------------------------------------------------");
        GraphDatabaseDAO graphDatabaseDAO2 = new GraphDatabaseDAO();
        outVideo.println("The most active user is: " + graphDatabaseDAO2.findMostActiveUser());
        outVideo.println("The most reviewed place is: " + graphDatabaseDAO2.findMostReviewedPlace());
        graphDatabaseDAO2.close();

        outVideo.println("-----------------------------------------------------------------------------------");
        outVideo.println("This is the history of the application (the mean rate by year and category):");
        HashMap<String, String> history = reviewDAO.findHistoryOfCategories(docDatabase);
        ListsManagement.showValues(history);

        outVideo.println("-----------------------------------------------------------------------------------");
        outVideo.println("This is the mean rate reached by each category:");
        HashMap<String, String> ratedCategories = reviewDAO.findMeanRatePerCategory(docDatabase);
        ListsManagement.showValues(ratedCategories);

        outVideo.println("-----------------------------------------------------------------------------------");
        outVideo.println("Top 10 most rated places:");
        HashMap<String, String> bestPlaces = reviewDAO.findTopTenPlaces(docDatabase);
        ListsManagement.showResults(bestPlaces);
        outVideo.println("-----------------------------------------------------------------------------------");

    }

    public static void main(String[] args) {
        Main m = new Main();
    }
}

