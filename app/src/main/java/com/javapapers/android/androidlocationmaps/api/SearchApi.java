package com.javapapers.android.androidlocationmaps.api;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

import java.util.List;

public interface SearchApi {
    @GET("/services/rest/?method=flickr.photos.search&api_key=e9a06935cacbaa16a9aeb7cc0137b859&format=json&nojsoncallback=1")
    Call<FlickrPhotos> getPhotos(
            @Query("lat") double latitude,
            @Query("lon") double longitude
    );
}
