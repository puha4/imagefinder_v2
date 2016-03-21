package com.javapapers.android.androidlocationmaps.api;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

public interface SearchApi {

    @GET("/services/rest/?method=flickr.auth.getToken&api_key=5f45c46eaf6e87b55c9f36fec03e3466")
    Call<String> getToken(
            @Query("frob") String frob,
            @Query("api_sig") String apiSig
    );

    @GET("/services/rest/?method=flickr.photos.search&api_key=5f45c46eaf6e87b55c9f36fec03e3466&format=json&nojsoncallback=1")
    Call<FlickrPhotos> getPhotos(
            @Query("lat") double latitude,
            @Query("lon") double longitude
    );
}
