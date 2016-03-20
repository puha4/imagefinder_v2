package com.javapapers.android.androidlocationmaps;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.jake.quiltview.QuiltView;
import com.javapapers.android.androidlocationmaps.api.FlickrPhotos;
import com.javapapers.android.androidlocationmaps.api.Photo;
import com.javapapers.android.androidlocationmaps.api.SearchApi;
import com.javapapers.android.androidlocationmaps.fragment.GoogleMapFragment;
import com.squareup.picasso.Picasso;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import retrofit.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private double latitude;
    private double longitude;

    private FragmentManager manager;
    private GoogleMapFragment googleMapFragment;

    private QuiltView quiltView;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_location_google_map);
        initToolbar();

        manager = getSupportFragmentManager();
        googleMapFragment = new GoogleMapFragment();

        if (savedInstanceState == null) {
            manager.beginTransaction().add(R.id.mapContainer, googleMapFragment).commit();
        }

        quiltView = (QuiltView) findViewById(R.id.quilt);
        quiltView.setChildPadding(1);

        Uri uri = getIntent().getData();

        if (uri != null) {
            String frob = uri.getQueryParameter("frob");
            Log.i(TAG+"frob", frob);
        }
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
    }

    public void addTestQuilts(int num){
        ArrayList<ImageView> images = new ArrayList<ImageView>();
        for(int i = 0; i < num; i++){
            ImageView image = new ImageView(this.getApplicationContext());
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if(i % 2 == 0)
                image.setImageResource(R.drawable.mayer);
            else
                image.setImageResource(R.drawable.mayer1);
            images.add(image);
        }
        quiltView.addPatchImages(images);
    }

    public void sendRequest() {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.flickr.com")
                .build();

        SearchApi searchApi = retrofit.create(SearchApi.class);

        this.latitude = googleMapFragment.getLatitude();
        this.longitude = googleMapFragment.getLongitude();

        Call<FlickrPhotos> call = searchApi.getPhotos(latitude, longitude);

        call.enqueue(new Callback<FlickrPhotos>() {
            @Override
            public void onResponse(Response<FlickrPhotos> response, Retrofit retrofit) {
                List<Photo> photos = response.body().getPhotos().getPhoto();
                Collections.shuffle(photos);
                List<Photo> subPhotos = photos.subList(0, 15);

                ArrayList<ImageView> images = new ArrayList<ImageView>();

                for (Photo photo : subPhotos) {
                    ImageView image = new ImageView(MainActivity.this);
                    image.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    Picasso.with(getApplicationContext())
                            .load("https://farm" + photo.getFarm() + ".staticflickr.com/" + photo.getServer() + "/" + photo.getId() + "_" + photo.getSecret() + ".jpg")
                            .fit()
                            .centerInside()
                            .into(image);

                    images.add(image);
                }

                quiltView.addPatchImages(images);
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(MainActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        String apiSig2 = new String(Hex.encodeHex(DigestUtils.md5("6e732ab8487b05e4api_key5f45c46eaf6e87b55c9f36fec03e3466permswrite")));

        Log.i(TAG + "apiSig2", apiSig2);

//        Call<String> callAuth = searchApi.doAuth(apiSig2);
//        callAuth.enqueue(new Callback<String>() {
//            @Override
//            public void onResponse(Response<String> response, Retrofit retrofit) {
//                Log.i(TAG+"auth", response.toString());
//                Log.i(TAG+"auth", response.body().toString());
//            }
//
//            @Override
//            public void onFailure(Throwable t) {
//                Toast.makeText(MainActivity.this, "auth "+t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
//            }
//        });
    }

    public void onAuth(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.flickr.com/services/auth/?api_key=5f45c46eaf6e87b55c9f36fec03e3466&perms=write&api_sig=9e61f86f3d3f8caf024a8bd1729d4ff3")));
    }
}