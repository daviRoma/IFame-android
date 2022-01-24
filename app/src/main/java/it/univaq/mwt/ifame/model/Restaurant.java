package it.univaq.mwt.ifame.model;

import java.io.Serializable;
import java.util.List;

public class Restaurant  implements Serializable {

    private String id;

    private String name;

    private double latitude;

    private double longitude;

    private String city;

    private String state;

    private String address;

    private String image;

    private List<String> categories;

    public Restaurant() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) throws AssertionError {
        assert id != null;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws AssertionError  {
        assert name != null;
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) throws AssertionError  {
        assert latitude != null;
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) throws AssertionError  {
        assert longitude != null;
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) throws AssertionError  {
        assert address != null;
        this.address = address;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCity() { return this.city; }

    public void setCity(String city) { this.city = city; }

    public String getState() { return  this.state; }

    public void setState(String state) { this.state = state; }

    public List<String> getCategories() { return this.categories; }

    public void setCategories(List<String> categories) { this.categories = categories; }
}
