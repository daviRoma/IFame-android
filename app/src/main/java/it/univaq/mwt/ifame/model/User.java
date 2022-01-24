package it.univaq.mwt.ifame.model;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {

    public String id;

    public String name;

    public String surname;

    public String username;

    public String email;

    public String avatar;

    public List<String> preferences;

    public User() {
    }

}