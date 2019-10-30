package com.example.momen.uber_clone;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ViewRequests extends AppCompatActivity implements LocationListener{

    ListView listView;
    ArrayList<String> arrayList;
    ArrayList<String> userName;
    ArrayList<Double> latatude;
    ArrayList<Double> longiude;
    ArrayAdapter arrayAdapter;
    LocationManager locationManager;
    String provider;
    Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);
        listView = (ListView) findViewById(R.id.listView);
        arrayList = new ArrayList<String>();
        userName = new ArrayList<String>();
        latatude = new ArrayList<Double>();
        longiude = new ArrayList<Double>();
        arrayList.add("find nearby requests ....");
        arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(arrayAdapter);
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
            updateLocation(location);
        }else {
            Toast.makeText(this, "Location is Null", Toast.LENGTH_SHORT).show();
            Log.i("Location ","null");
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(),ViewRiderLocation.class);
                intent.putExtra("userName",userName.get(position));
                intent.putExtra("latitude",latatude.get(position));
                intent.putExtra("longitude",longiude.get(position));
                intent.putExtra("userLatitude",location.getLatitude());
                intent.putExtra("userLongitude",location.getLongitude());

                startActivity(intent);

            }
        });

    }
    public void updateLocation(final Location location){

        final ParseGeoPoint userLocation = new ParseGeoPoint(location.getLatitude(),location.getLongitude());
        ParseUser.getCurrentUser().put("location",userLocation);
        ParseUser.getCurrentUser().saveInBackground();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Requests");
        query.whereNear("requesterLocation", userLocation);
        query.setLimit(10);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){
                    if(objects.size()>0){
                        arrayList.clear();
                        userName.clear();
                        latatude.clear();
                        longiude.clear();
                        for(ParseObject object : objects){

                            if(object.get("driverUserName")== null) {

                                Double distance = userLocation.distanceInMilesTo((ParseGeoPoint) object.get("requesterLocation"));
                                Double distanceOnDB = (double) Math.round(distance * 10) / 10;
                                arrayList.add(distanceOnDB.toString() +"  Miles");
                                userName.add(object.getString("requesterUserName"));
                                latatude.add(object.getParseGeoPoint("requesterLocation").getLatitude());
                                longiude.add(object.getParseGeoPoint("requesterLocation").getLongitude());
                            }

                         }

                         arrayAdapter.notifyDataSetChanged();


                    }
                }

            } });

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
        //locationManager.removeUpdates(this);
    }
}
