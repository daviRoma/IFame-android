package it.univaq.mwt.ifame.utility;

public abstract class RestAPI {
    private static final String IFAME_URL = "http://10.0.2.2:8080/ifameService/ifame";
    private static final String AUTH_URL = "http://10.0.2.2:8080/ifameService/authentication";
    private static final String ACCOUNT_URL = "http://10.0.2.2:8080/accountService/account";

    public interface Ifame {
        String GET_EVENT = IFAME_URL + "/event/";
        String GET_EVENTS = IFAME_URL + "/events/all";
        String GET_EVENTS_JOINED = IFAME_URL + "/events/joined/";
        String GET_EVENTS_OWNER = IFAME_URL + "/events/owner/";
        String GET_RESTAURANTS = IFAME_URL + "/restaurants/all";
        String GET_FOODCATEGORIES = IFAME_URL + "/foodcategories/all";
        String EVENT_CREATE = IFAME_URL + "/event/create";
        String EVENT_UPDATE = IFAME_URL + "event/update/";
        String EVENT_DELETE = IFAME_URL + "/event/delete/";
        String JOIN = IFAME_URL + "/participation/event/join";
        String PARTICIPATION_REMOVE = IFAME_URL + "/participation/event/remove/";
    }

    public interface Auth {
        String LOGIN = AUTH_URL + "/login";
        String VALIDATE_TOKEN = AUTH_URL + "/validate/token";
    }

    public interface Account {
        String GET = ACCOUNT_URL + "/";
        String REGISTER = ACCOUNT_URL + "/register";
        String UPDATE = ACCOUNT_URL + "/update/";
    }

}
