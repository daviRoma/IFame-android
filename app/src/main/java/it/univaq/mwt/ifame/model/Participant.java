package it.univaq.mwt.ifame.model;

import java.io.Serializable;

public class Participant implements Serializable {

    private String username;

    public Participant(String username) {
        this.username = username;
    }

    public Participant() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) throws AssertionError{
        assert username != null;
        this.username = username;
    }

}
