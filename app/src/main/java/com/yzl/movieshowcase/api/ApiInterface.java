package com.yzl.movieshowcase.api;

import com.yzl.movieshowcase.model.SearchItem;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    //此处不能为空，若api网址后缀确为空，此处应加空格
    @GET(" ")
    Observable<SearchItem> getSearchItems(
            @Query("apikey") String apikey,
            @Query("s") String query,
            @Query("page") String page);

}
