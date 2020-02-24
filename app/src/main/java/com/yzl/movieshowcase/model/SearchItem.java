package com.yzl.movieshowcase.model;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class SearchItem implements Serializable{
    @SerializedName("Response")
    @Expose
    private boolean Response;
    @SerializedName("totalResults")
    @Expose
    private int totalResults;
    @SerializedName("Search")
    @Expose
    private List<Item> Search = null;

    public boolean isResponse() {
        return Response;
    }

    public void setResponse(boolean response) {
        Response = response;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public List<Item> getSearch() {
        return Search;
    }

    public void setSearch(List<Item> search) {
        Search = search;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }
}
