package com.javapapers.android.androidlocationmaps;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.javapapers.android.androidlocationmaps.api.FlickrPhotos;
import com.javapapers.android.androidlocationmaps.api.Photo;
import com.javapapers.android.androidlocationmaps.api.SearchApi;
import com.javapapers.android.androidlocationmaps.fragment.GoogleMapFragment;
import retrofit.*;

public class MainActivity extends FragmentActivity implements
        Callback<FlickrPhotos> {

    private static final String TAG = "MainActivity";

    private double latitude;
    private double longitude;

    private FragmentManager manager;
    private GoogleMapFragment googleMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate ...............................");

        setContentView(R.layout.activity_location_google_map);

        manager = getSupportFragmentManager();
        googleMapFragment = new GoogleMapFragment();

        if (savedInstanceState == null) {
            manager.beginTransaction().add(R.id.mapContainer, googleMapFragment).commit();
        }

    }

    // retrofit
    public void onSearchClick(View view) {

//        Gson gson = new GsonBuilder().create();

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.flickr.com")
                .build();


        SearchApi searchApi = retrofit.create(SearchApi.class);


        this.latitude = googleMapFragment.getLatitude();
        this.longitude = googleMapFragment.getLongitude();
        Log.i(TAG, latitude+" "+ longitude);

        Call<FlickrPhotos> call = searchApi.getPhotos(latitude, longitude);

//        try {
//            Response<FlickrPhotos> response = call.execute();
//            Log.i(TAG, latitude+" "+ longitude);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        call.enqueue(this);
    }

    @Override
    public void onResponse(Response<FlickrPhotos> response, Retrofit retrofit) {
        Log.i(TAG, response.toString()+ " ");
        for (Photo photo : response.body().getPhotos().getPhoto()) {
            Log.i(TAG, photo.toString());
        }
    }

    @Override
    public void onFailure(Throwable t) {
        Toast.makeText(this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }
}