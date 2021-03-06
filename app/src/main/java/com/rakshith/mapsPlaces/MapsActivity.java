package com.rakshith.mapsPlaces;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;

import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener
        {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    int PROXIMITY_RADIUS = 100000;
    double latitude, longitude;
    double end_latitude, end_longitude;
    private DatabaseHelper db;
    ArrayList<Marker> markerArrayList = new ArrayList<>();
    boolean isUpdateClicked = false;
   String previousLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       db = new DatabaseHelper(this);
        setContentView(R.layout.activity_maps);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        //Check if Google Play Services Available or not
        if (!CheckGooglePlayServices()) {
            Log.d("onCreate", "Finishing test case since Google Play Services are not available");
            finish();
        }
        else {
            Log.d("onCreate","Google Play Services available.");
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private boolean CheckGooglePlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        0).show();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);

        mMap.setOnMapLongClickListener(new
                                               GoogleMap.OnMapLongClickListener() {
                                                   @Override
                                                   public void onMapLongClick (LatLng latLng){
                                                       mMap.clear();
                                                       isUpdateClicked = false;
                                                       Geocoder geocoder =
                                                               new Geocoder(MapsActivity.this);
                                                       List<Address> list;
                                                       try {
                                                           list = geocoder.getFromLocation(latLng.latitude,
                                                                   latLng.longitude, 1);
                                                       } catch (IOException e) {
                                                           return;
                                                       }
                                                       Address address = list.get(0);

                                                    for(int i=0 ;i<markerArrayList.size();i++){
                                                           markerArrayList.get(i).remove();
                                                    }
                                                    markerArrayList.clear();

                                                       MarkerOptions options = new MarkerOptions()
                                                               .title(list.get(0).getPremises() +
                                                                       " "+list.get(0).getLocality()+" "+ list.get(0).getAdminArea()+
                                                                       " "+list.get(0).getFeatureName()+" " +list.get(0).getSubLocality()
                                                                       +" "+list.get(0).getCountryName())
                                                               .position(new LatLng(latLng.latitude,
                                                                       latLng.longitude));

                                                       markerArrayList.add( mMap.addMarker(options));

                                                   }
                                               });


    }



    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public void onClick(View v)
    {

        Object dataTransfer[] = new Object[2];
        Geocoder geocoder = new Geocoder(this);
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData(geocoder);


        switch(v.getId()) {
            case R.id.B_search: {
                mMap.clear();
                isUpdateClicked = false;
                EditText tf_location = (EditText) findViewById(R.id.TF_location);
                String location = tf_location.getText().toString();
                List<Address> addressList = null;
                MarkerOptions markerOptions = new MarkerOptions();
                Log.d("location = ", location);

                if (!location.equals("")) {

                    try {
                        addressList = geocoder.getFromLocationName(location, 5);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (addressList != null) {
                        for (int i = 0; i < addressList.size(); i++) {
                            Address myAddress = addressList.get(i);
                            LatLng latLng = new LatLng(myAddress.getLatitude(), myAddress.getLongitude());
                            markerOptions.position(latLng);
                            markerOptions.title(myAddress.getPremises() +
                                    " "+myAddress.getLocality()+" "+ myAddress.getAdminArea()+
                                    " "+myAddress.getFeatureName()+" " +myAddress.getSubLocality()
                                    +" "+myAddress.getCountryName());
                             mMap.addMarker(markerOptions);


                            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                        }
                    }

                }
            }
            break;
            case R.id.B_hospital:
                mMap.clear();
                isUpdateClicked = false;
                String hospital = "hospital";
                String url = getUrl(latitude, longitude, hospital);

                dataTransfer[0] = mMap;
                dataTransfer[1] = url;

                getNearbyPlacesData.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "Showing Nearby Hospitals", Toast.LENGTH_LONG).show();
                break;

            case R.id.B_restaurant:
                mMap.clear();
                isUpdateClicked = false;
                dataTransfer = new Object[2];
                String restaurant = "restaurant";
                url = getUrl(latitude, longitude, restaurant);
                getNearbyPlacesData = new GetNearbyPlacesData(geocoder);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;

                getNearbyPlacesData.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "Showing Nearby Restaurants", Toast.LENGTH_LONG).show();
                break;
            case R.id.B_school:
                mMap.clear();
                isUpdateClicked = false;
                String school = "school";
                dataTransfer = new Object[2];
                url = getUrl(latitude, longitude, school);
                getNearbyPlacesData = new GetNearbyPlacesData(geocoder);
                dataTransfer[0] = mMap;
                dataTransfer[1] = url;

                getNearbyPlacesData.execute(dataTransfer);
                Toast.makeText(MapsActivity.this, "Showing Nearby Schools", Toast.LENGTH_LONG).show();
                break;

            case R.id.B_to:

                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select Favourite Location");
                builder.setCancelable(true);

                final ListView modeList = new ListView(this);
                final ArrayList<String> stringAddress = new ArrayList<>();
             ArrayList<Favourite> favourites=    db.getAllContacts();
                for(int i=0; i<favourites.size();i++){
                    stringAddress.add(favourites.get(i).locationAddress);
                }
                final MapsActivity m  = this;
                final ArrayAdapter<Object> modeAdapter = new ArrayAdapter<Object>(this, android.R.layout.simple_list_item_1, android.R.id.text1, stringAddress.toArray());
                modeList.setAdapter(modeAdapter);
                builder.setView(modeList);
                final Dialog dialog = builder.create();
                dialog.show();
                final SwipeDetector swipeDetector = new SwipeDetector(modeList);
                modeList.setOnTouchListener(swipeDetector);


                modeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        System.out.println("inside on item click listener");


                        if (swipeDetector.swipeDetected()){
                            System.out.println("swipeDetector.getAction().toString()===>"+swipeDetector.getAction().toString());
                            if(swipeDetector.getAction().toString().equalsIgnoreCase("RL")){
                                isUpdateClicked = true;
                                mMap.clear();
                                TextView selected = (TextView) view;

                                Favourite fav =      db.getContact(selected.getText().toString());

                                previousLocation = selected.getText().toString();

                                dialog.cancel();
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(new LatLng(fav.getLatitude(),fav.getLongitude()));
                                markerOptions.title(previousLocation);
                                mMap.addMarker(markerOptions);


                            }else if(swipeDetector.getAction().toString().equalsIgnoreCase("LR")){
                                isUpdateClicked = false;
                                Favourite favourite = new Favourite();
                                TextView selected = (TextView) view;

                                favourite.setLocationAddress(selected.getText().toString());
                                view.setBackgroundColor(Color.RED);



                                db.deleteContact(favourite);
                                View myView = findViewById(R.id.B_to);
                                dialog.cancel();
                                myView.performClick();



                                Toast.makeText(MapsActivity.this,"Your Favourite Location is deleted", Toast.LENGTH_LONG).show();
                            }
                            modeList.invalidateViews();
                            modeAdapter.notifyDataSetChanged();

                            System.out.println(swipeDetector.getAction().toString()+"===>");
                            System.out.println("swipe detected");


                        } else {
                            TextView selected = (TextView) view;
                            isUpdateClicked = true;
                            mMap.clear();
                            Favourite fav  =      db.getContact(selected.getText().toString());

                            Object dataTransfer[]  = new Object[3];
                            end_latitude = fav.getLatitude();
                            end_longitude = fav.getLongitude();
                            String url = getDirectionsUrl();
                            System.out.println(url);

                            GetDirectionsData getDirectionsData = new GetDirectionsData();
                            dataTransfer[0] = mMap;
                            dataTransfer[1] = url;
                            dataTransfer[2] = new LatLng(end_latitude, end_longitude);
                            getDirectionsData.execute(dataTransfer);
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(new LatLng(fav.getLatitude(),fav.getLongitude()));
                            markerOptions.title(fav.getLocationAddress());

                            mMap.addMarker(markerOptions);

                            dialog.cancel();

                        }
                    }
                });

                        break;

            case R.id.maps:

                AlertDialog.Builder mapBuilder = new AlertDialog.Builder(this);
                mapBuilder.setTitle("Select Map type");
                mapBuilder.setCancelable(true);

                final ListView listView = new ListView(this);
                String[] stringArray = new String[] {"Default","Satellite","Terrain"};
                final ArrayAdapter<Object> mapAdapter = new ArrayAdapter<Object>(this, android.R.layout.simple_list_item_1, android.R.id.text1, stringArray);
                listView.setAdapter(mapAdapter);

                mapBuilder.setView(listView);

                final Dialog alertDialog = mapBuilder.create();

                alertDialog.show();


                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        TextView selected = (TextView) view;
                        if(selected.getText().toString().equalsIgnoreCase("Terrain")){
                            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        }else if(selected.getText().toString().equalsIgnoreCase("Satellite")){
                            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        }else{
                            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        }
                        alertDialog.cancel();
                        System.out.println(selected.getText());

                    }
                });

                break;


        }
    }



    private String getDirectionsUrl()
    {
        StringBuilder googleDirectionsUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionsUrl.append("origin="+latitude+","+longitude);
        googleDirectionsUrl.append("&destination="+end_latitude+","+end_longitude);
        googleDirectionsUrl.append("&key="+"AIzaSyC2svTb8UgzER69huJB70dv_ixPKTCJi7E");

        return googleDirectionsUrl.toString();
    }

    private String getUrl(double latitude, double longitude, String nearbyPlace)
    {
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + PROXIMITY_RADIUS);
        googlePlacesUrl.append("&type=" + nearbyPlace);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=AIzaSyC2svTb8UgzER69huJB70dv_ixPKTCJi7E");
        Log.d("getUrl", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }




    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }



    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("onLocationChanged", "entered");

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        latitude = location.getLatitude();
        longitude = location.getLongitude();


        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.draggable(true);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));


        Toast.makeText(MapsActivity.this,"Your Current Location", Toast.LENGTH_LONG).show();


        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            Log.d("onLocationChanged", "Removing Location Updates");
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }


            @Override
            public boolean onMarkerClick(final Marker marker) {
                marker.setDraggable(true);
                /*if(marker.getTitle().equalsIgnoreCase("Current Position")){

                }*/
                if(!isUpdateClicked){
                    new AlertDialog.Builder(this)
                            .setTitle("Add to favourites")
                            .setMessage("Are you sure you want to "+marker.getTitle()+"add this location to your favourites?")

                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Favourite favourite = new Favourite();
                                    favourite.setLatitude(marker.getPosition().latitude);
                                    favourite.setLongitude(marker.getPosition().longitude);
                                    favourite.setLocationAddress(marker.getTitle());

                                    if(previousLocation != null){
                                        db.updateContact(favourite,previousLocation);
                                        previousLocation = null;

                                        Toast.makeText(MapsActivity.this, "Location has been updated succesfully.", Toast.LENGTH_LONG).show();
                                        isUpdateClicked = true;
                                    }else{
                                        db.insertContact(favourite);

                                    }

                                }
                            })

                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }

                return false;
            }

            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
             marker.hideInfoWindow();
                Geocoder geocoder =
                        new Geocoder(MapsActivity.this);

                List<Address> list;
                try {
                    list = geocoder.getFromLocation(marker.getPosition().latitude,
                            marker.getPosition().longitude, 1);
                } catch (IOException e) {
                    return;
                }

                marker.setTitle(list.get(0).getPremises() +" "+list.get(0).getLocality()+" "+ list.get(0).getAdminArea()+" "+list.get(0).getFeatureName()+" " +list.get(0).getSubLocality() +" "+list.get(0).getCountryName());
                end_latitude = marker.getPosition().latitude;
                end_longitude =  marker.getPosition().longitude;

                Log.d("end_lat",""+end_latitude);
                Log.d("end_lng",""+end_longitude);

                isUpdateClicked = false;



            }
        }


