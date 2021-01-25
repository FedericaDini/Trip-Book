package it.unipi.aide.Beans;

import java.util.ArrayList;

public class Place {
    private String id;
    private String name;
    private String address;
    private String location;
    private String main_category;
    private ArrayList<String> categories;
    private String latitude;
    private String longitude;
    private String telephone;
    private String website;
    private double recentRate;
    private ArrayList<Review> recentReviews;

    public Place(String id, String name, String address, String location, String main_category, ArrayList<String> categories, String latitude, String longitude, String telephone, String website, double recentRate, ArrayList<Review> recentReviews) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.location = location;
        this.main_category = main_category;
        this.categories = categories;
        this.latitude = latitude;
        this.longitude = longitude;
        this.telephone = telephone;
        this.website = website;
        this.recentRate = recentRate;
        this.recentReviews = recentReviews;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMain_category() {
        return main_category;
    }

    public void setMain_category(String main_category) {
        this.main_category = main_category;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public double getRecentRate() {
        return recentRate;
    }

    public void setRecentRate(double recentRate) {
        this.recentRate = recentRate;
    }

    public ArrayList<Review> getRecentReviews() {
        return recentReviews;
    }

    public void setRecentReviews(ArrayList<Review> recentReviews) {
        this.recentReviews = recentReviews;
    }

    @Override
    public String toString() {

        String s = "Id: " + id + "\n" +
                "Name: " + name + "\n" +
                "Main category: " + main_category + "\n";

        if (!(categories == null) && !(categories.isEmpty())) {
            s = s.concat("Other categories: " + categories + "\n");
        }

        if (!(address == null) && !address.isEmpty()) {
            s = s.concat("Address: " + address + "\n");
        }

        if (!(location == null) && !(location.isEmpty())) {
            s = s.concat("Location: " + location + "\n");
        }

        if (!(latitude == null) && !(latitude.isEmpty())) {
            s = s.concat("Latitude: " + latitude + "\n");
        }

        if (!(longitude == null) && !(longitude.isEmpty())) {
            s = s.concat("Longitude: " + longitude + "\n");
        }

        if (!(telephone == null) && !(telephone.isEmpty())) {
            s = s.concat("Phone number: " + telephone + "\n");
        }

        if (!(website == null) && !(website.isEmpty())) {
            s = s.concat("Website: " + website + "\n");
        }

        if (recentRate != 0) {
            s = s.concat("Rate from recent reviews: " + recentRate + "\n" +
                    printRecentReviews());
        } else {
            s = s.concat("No reviews available for the place");
        }

        return s;
    }

    public String printRecentReviews() {
        String result = "------------------------------------------\nReviews:\n";
        for (Review r : recentReviews) {
            result = result.concat("------------------------------------------\n" + r.toString() + "\n");
        }
        return result;
    }
}
