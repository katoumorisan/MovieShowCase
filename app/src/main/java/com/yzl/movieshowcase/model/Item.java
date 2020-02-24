package com.yzl.movieshowcase.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Item implements Serializable {

    @SerializedName("Year")
    @Expose
    private String Year;
    @SerializedName("Title")
    @Expose
    private String Title;
    @SerializedName("Poster")
    @Expose
    private String Poster;
    @SerializedName("Type")
    @Expose
    private String Type;
    @SerializedName("imdbID")
    @Expose
    private String imdbID;

    public String getYear() {
        return Year;
    }

    public void setYear(String year) {
        Year = year;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getPoster() {
        return Poster;
    }

    public void setPoster(String poster) {
        Poster = poster;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getImdbID() {
        return imdbID;
    }

    public void setImdbID(String imdbID) {
        this.imdbID = imdbID;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }
}
