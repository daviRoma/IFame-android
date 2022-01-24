package it.univaq.mwt.ifame.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

public class Category implements Serializable {

    @PrimaryKey
    @NonNull
    public String id;

    public String title;

    @ColumnInfo(name = "title_it")
    public String titleIt;

    public Category() {
    }

    @NonNull
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

    public String getTitleIt() {
        return titleIt;
    }

    public void setTitleIt(String titleIt) throws AssertionError {
        assert titleIt != null;
        this.titleIt = titleIt;
    }
}
