package it.unipi.aide.Beans;

public class User {
    private String username;
    private String password;
    private String city;
    private String province;
    private String type;

    public User(String username, String password, String city, String province, String type) {
        this.username = username;
        this.password = password;
        this.city = city;
        this.province = province;
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {

        String s = "Username: " + username + "\n" +
                "Password: " + password + "\n" +
                "Type: " + type + "\n";

        if (!(city == null) && !(city.isEmpty())) {
            s = s.concat("City: " + city + "\n");
        }

        if (!(province == null) && !province.isEmpty()) {
            s = s.concat("Province: " + province + "\n");
        }

        return s;
    }
}
