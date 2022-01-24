package it.univaq.mwt.ifame.utility;

public enum PreferenceKey {

    USER_LATITUDE("user_latitude"),
    USER_LONGITUDE("user_longitude"),
    USER_CITY("user_city");

    private final String text;

    PreferenceKey(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

}
