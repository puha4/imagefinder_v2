package com.javapapers.android.androidlocationmaps;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
//import com.daimajia.slider.library.SliderLayout;
//import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.android.ui.IconGenerator;
import com.javapapers.android.androidlocationmaps.api.FlickrPhotos;
import com.javapapers.android.androidlocationmaps.api.Photo;
import com.javapapers.android.androidlocationmaps.api.SearchApi;
import retrofit.*;


import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public class LocationActivity extends FragmentActivity implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        Callback<FlickrPhotos> {

    private static final String TAG = "LocationActivity";
    private static final long INTERVAL = 1000 * 60 * 1; //1 minute
    private static final long FASTEST_INTERVAL = 1000 * 60 * 1; // 1 minute

    private double latitude;
    private double longitude;

    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;
    GoogleMap googleMap;

//    private SliderLayout sliderShow;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate ...............................");

        if (!isGooglePlayServicesAvailable()) {
            finish();
        }

        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        setContentView(R.layout.activity_location_google_map);

        // slider start
//        sliderShow = (SliderLayout) findViewById(R.id.slider);
//
//        TextSliderView textSliderView = new TextSliderView(this);
//        textSliderView
//                .description("Game of Thrones")
//                .image("http://images.boomsbeat.com/data/images/full/19640/game-of-thrones-season-4-jpg.jpg");
//
//        sliderShow.addSlider(textSliderView);
        // slider end

        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        googleMap = fm.getMap();
        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart connect ..............");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        addMarker();
    }

    private void addMarker() {
        googleMap.clear();

        MarkerOptions options = new MarkerOptions();

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setStyle(IconGenerator.STYLE_PURPLE);
        options.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(mLastUpdateTime)));
        options.anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());


        this.latitude = mCurrentLocation.getLatitude();
        this.longitude = mCurrentLocation.getLongitude();

        LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        options.position(currentLatLng);
//        Marker mapMarker = googleMap.addMarker(options);


        long atTime = mCurrentLocation.getTime();
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date(atTime));

        Marker mapMarker = googleMap.addMarker(new MarkerOptions()
                .position(currentLatLng)
                .title(mLastUpdateTime)
                .snippet("You are here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//        mapMarker.setTitle(mLastUpdateTime);
        Log.d(TAG, "Marker added.............................");
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 13));
        Log.d(TAG, "Zoom done.............................");
    }

    @Override
    public void onStop() {
//        sliderShow.stopAutoCycle();
        super.onStop();
        Log.d(TAG, "onStop disconnect ..............");
        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            Log.d(TAG, "Location update stopped .......................");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    // retrofit
    public void onSearchClick(View view) {

//        Gson gson = new GsonBuilder().create();

        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.flickr.com")
                .build();


        SearchApi searchApi = retrofit.create(SearchApi.class);

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
        for (Photo photo : response.body().photos.getPhoto()) {
            Log.i(TAG, photo.toString());
        }
    }

    @Override
    public void onFailure(Throwable t) {
        Toast.makeText(this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }
}