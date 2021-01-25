package it.unipi.aide.Beans;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Review {
    private String id;
    private Date date;
    private double rate;
    private boolean polarity;
    private String text;
    private String title;
    private String user;
    private String place_id;
    private String place_name;
    private String place_category;

    public Review() {
    }

    public Review(String id, Date date, double rate, boolean polarity, String text, String title, String user, String place_id, String place_name, String place_category) {
        this.id = id;
        this.date = date;
        this.rate = rate;
        this.polarity = polarity;
        this.text = text;
        this.title = title;
        this.user = user;
        this.place_id = place_id;
        this.place_name = place_name;
        this.place_category = place_category;
    }

    public Review(Date date, double rate, boolean polarity, String text, String title, String user, String place_id, String place_name, String place_category) {
        this.date = date;
        this.rate = rate;
        this.polarity = polarity;
        this.text = text;
        this.title = title;
        this.user = user;
        this.place_id = place_id;
        this.place_name = place_name;
        this.place_category = place_category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public boolean isPolarity() {
        return polarity;
    }

    public void setPolarity(boolean polarity) {
        this.polarity = polarity;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public String getPlace_name() {
        return place_name;
    }

    public void setPlace_name(String place_name) {
        this.place_name = place_name;
    }

    public String getPlace_category() {
        return place_category;
    }

    public void setPlace_category(String place_category) {
        this.place_category = place_category;
    }

    @Override
    public String toString() {
        String pattern = "dd/MM/yyyy HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        String s = "Review made on the " + simpleDateFormat.format(date) +
                " by " + user + "about " + place_name + "\nTitle: " + title + "\n" +
                "Text: " + text + "\n" +
                "Rate: " + rate + "\n";

        if (polarity) {
            s = s.concat("Sentiment: positive");
        } else {
            s = s.concat("Sentiment: negative");
        }

        return s;
    }
}
