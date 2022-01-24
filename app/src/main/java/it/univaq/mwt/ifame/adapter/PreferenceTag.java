package it.univaq.mwt.ifame.adapter;

import java.io.Serializable;

public class PreferenceTag implements Serializable {

    private String category;
    private boolean isChecked = false;

    public PreferenceTag() { }

    public PreferenceTag(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String name) {
        this.category = name;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}