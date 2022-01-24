package it.univaq.mwt.ifame.model;

import java.io.Serializable;

public class Event implements Serializable {

    private String id;

    private String title;

    private String day;

    private String hour;

    private String message;

    private String image;

    private long maxParticipants;

    private String idAuthor;

    private String idRestaurant;

    public Event() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) throws AssertionError {
        assert id != null;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) throws AssertionError {
        assert title != null;
        this.title = title;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) throws AssertionError {
        assert day != null;
        this.day = day;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) throws AssertionError {
        assert hour != null;
        this.hour = hour;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Long maxParticipants) throws AssertionError {
        assert maxParticipants != null;
        this.maxParticipants = maxParticipants;
    }

    public String getIdAuthor() {
        return idAuthor;
    }

    public void setIdAuthor(String idAuthor) throws AssertionError {
        assert idAuthor != null;
        this.idAuthor = idAuthor;
    }

    public String getIdRestaurant() {
        return idRestaurant;
    }

    public void setIdRestaurant(String idRestaurant) throws AssertionError {
        assert idRestaurant != null;
        this.idRestaurant = idRestaurant;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
