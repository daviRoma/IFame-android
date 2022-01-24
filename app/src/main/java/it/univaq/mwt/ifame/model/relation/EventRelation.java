package it.univaq.mwt.ifame.model.relation;

import java.io.Serializable;
import java.util.List;

import it.univaq.mwt.ifame.model.Event;

import it.univaq.mwt.ifame.model.Participant;
import it.univaq.mwt.ifame.model.Restaurant;

public class EventRelation implements Serializable {

    public Event event;

    public Restaurant restaurant;

    public List<Participant> participants;

}
