package com.example.momen.uber_clone;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class YourLocation extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    String provider;
    Location location;
    double lat;
    double lang;

    TextView textView;
    Button button;
    boolean requested = false;
    private Marker marker = null;
    String driverUserName = "";
    ParseGeoPoint driverLocation = new ParseGeoPoint(0,0);
    Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        textView = (TextView) findViewById(R.id.textView);
        button = (Button) findViewById(R.id.button2);

        Toast.makeText(this, ParseUser.getCurrentUser().getUsername(), Toast.LENGTH_SHORT).show();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;

        }
        locationManager.requestLocationUpdates(provider, 0, 1, this);
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            Log.d("Location ","Not null");
            //updateLocation(location);
        }else {
            Toast.makeText(this, "Location is Null", Toast.LENGTH_SHORT).show();
            Log.i("Location ","null");
        }


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(location != null) {
            lat = location.getLatitude();
            lang = location.getLongitude();
            //// Add a marker in Sydney and move the camera
            LatLng sydney = new LatLng(lat, lang);
            if(marker == null) {
                marker = mMap.addMarker(new MarkerOptions().position(sydney).title("your Location"));
            }
            else{
                marker.setPosition(sydney);
            }
            //mMap.addMarker(new MarkerOptions().position(sydney).title("Your Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }
        else{
            LatLng sydney = new LatLng(0, 0);
            mMap.addMarker(new MarkerOptions().position(sydney).title("no Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        }
        updateLocation(location);
    }
    public void updateLocation(final Location location) {

        if(requested == false){
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Requests");
            query.whereEqualTo("requesterUserName",ParseUser.getCurrentUser().getUsername());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e == null){
                        if(objects.size()>0){
                            for(ParseObject object : objects)
                            {
                                requested = true;
                                textView.setText("Find Uber Driver ...");
                                button.setText("Cancel Uber");
                                if(object.get("driverUserName") != null){
                                    driverUserName = object.getString("driverUserName");
                                    textView.setText("your drive is on their way.....");
                                    button.setVisibility(View.INVISIBLE);
                                }
                            }
                        }
                    }
                }
            });
        }


        lat = location.getLatitude();
        lang = location.getLongitude();
        LatLng sydney = new LatLng(lat, lang);
       if(driverUserName.equals("")){
            if (marker == null) {
                marker = mMap.addMarker(new MarkerOptions().position(sydney).title("your Location"));
            } else {
                marker.setPosition(sydney);
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }
        if (requested == true) {
            if(!driverUserName.equals("")){
                ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
                userQuery.whereEqualTo("username",driverUserName);
                userQuery.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> objects, ParseException e) {
                        if(e==null)
                        {
                            if(objects.size()>0){
                                for (ParseUser driver : objects){

                                    driverLocation = driver.getParseGeoPoint("location");
                                }
                            }
                        }
                    }
                });
                if (driverLocation.getLatitude()!= 0 && driverLocation.getLongitude() != 0){
                    Log.i("loc",driverLocation.toString());
                    Double distance = driverLocation.distanceInMilesTo(new ParseGeoPoint(location.getLatitude(),location.getLongitude()));
                    Double distanceOnDB = (double) Math.round(distance * 10) / 10;
                    textView.setText("your drive is on "+distance.toString()+" miles way. ..");
                    ArrayList<Marker> markers = new ArrayList<Marker>();
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    // Add a marker in Sydney and move the camera
                    LatLng sydney1 = new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude());

                    markers.add(mMap.addMarker(new MarkerOptions().position(sydney1).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title("Driver Location")));
                    markers.add(mMap.addMarker(new MarkerOptions().position(sydney).title("Your Location")));
                    for (Marker marker : markers) {
                        builder.include(marker.getPosition());
                    }
                    LatLngBounds bounds = builder.build();
                    int padding = 100; // offset from edges of the map in pixels
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    mMap.moveCamera(cu);
                    mMap.animateCamera(cu);
                }
            }
            final ParseGeoPoint userLocation = new ParseGeoPoint(lat, lang);
            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Requests");
            query.whereEqualTo("requesterUserName",ParseUser.getCurrentUser().getUsername());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        if (objects.size() > 0) {
                            for (ParseObject object : objects) {
                                object.put("requesterLocation", userLocation);
                                object.saveInBackground();
                            }
                        }
                    }
                }
            });


        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateLocation(location);
            }
        },2000);
    }

    @Override
    public void onLocationChanged(Location location) {
        updateLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }
    public void requestUber(View view){

        if(requested == false) {
            ParseObject object = new ParseObject("Requests");
            object.put("requesterUserName", ParseUser.getCurrentUser().getUsername());

            ParseACL parseACL = new ParseACL();
            parseACL.setPublicWriteAccess(true);
            parseACL.setPublicReadAccess(true);
            object.setACL(parseACL);

            object.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    textView.setText("Find Uber Driver ...");
                    button.setText("Cancel Uber");
                    requested = true;
                }
            });
        }else {
            textView.setText("Uber Canceld.");
            button.setText("Request Uber");
            requested = false;

            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Requests");
            query.whereEqualTo("requesterUserName",ParseUser.getCurrentUser().getUsername());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e == null) {
                        if(objects.size()>0){
                            for(ParseObject object : objects)
                                object.deleteInBackground();
                        }
                    }
                }
            });
        }

    }
}
