package com.equinox.qikdriver.Models;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.equinox.qikdriver.Utils.AppVolleyController;
import com.equinox.qikdriver.Utils.LocationPermission;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.Context.LOCATION_SERVICE;
import static com.equinox.qikdriver.Models.Constants.USER_METADATA;

/**
 * Created by mukht on 11/2/2016.
 */
public class DataHolder {

    private static DataHolder ourInstance = new DataHolder();
    public static DataHolder getInstance() {
        return ourInstance;
    }

    public Map<String,Place> placeMap = new Hashtable<>();
    private static String TAG = "DataHolder";
    private ImageLoader imageLoader = AppVolleyController.getInstance().getImageLoader();
    public static Hashtable<String,List<Order>> orderList = new Hashtable<>();
    //public static HashMap<String, List<GroceryItem>> groceryItemMapping;
    public static FirebaseDatabase database = FirebaseDatabase.getInstance();
    public static DatabaseReference userDatabaseReference = null, businessDatabaseReference = null;
    public static Location location = null;
    public static List<String> ownershipList = null;
    public static User currentUser = null;
    public static final Boolean lock = true;

    public Map<String,Place> getPlaceMap() {
        return placeMap;
    }
    public ImageLoader getImageLoader() {
        if (imageLoader == null) imageLoader = AppVolleyController.getInstance().getImageLoader();
        return imageLoader;
    }

    public static void fetchLocationMetadata(final Location location, final Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Geocoder coder = new Geocoder(context, Locale.ENGLISH);
                List<Address> results = null;
                if (currentUser == null)
                    currentUser = new User();
                try {
                    results = coder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    currentUser.setAddress(results.get(0));
                    String baseURL = "https://1-dot-qikexpress.appspot.com/_ah/api/countryoperations/v1/country/search?countryCode=";
                    JsonObjectRequest ratingsReq = new JsonObjectRequest(baseURL+currentUser.getAddress().getCountryCode(), null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, response.toString());
                            try {
                                if (response.has("items")) {
                                    JSONArray countryArray = response.getJSONArray("items");
                                    for (int i = 0; i < countryArray.length(); i++) {
                                        JSONObject countryItemObject = countryArray.getJSONObject(i);
                                        currentUser.setLocalCurrency(countryItemObject.getString("currencyCode"));
                                        currentUser.setLocalCurrencySymbol(countryItemObject.getString("currencySymbol"));
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            VolleyLog.d(TAG, "Error: " + error.getMessage());
                        }
                    });
                    AppVolleyController.getInstance().addToRequestQueue(ratingsReq);
                } catch (IOException e) {     }
                return null;
            }
        }.execute();
    }

    public void setRole(final String role) {
        userDatabaseReference.child("roles").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> userRoles = (List<String>) dataSnapshot.getValue();
                if (userRoles == null)
                    userRoles = new ArrayList<>();
                if (!userRoles.contains(role))
                    userDatabaseReference.child("roles").child(String.valueOf(userRoles.size())).setValue(role);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {          }
        });
    }

    public static void generateMetadata() {
        userDatabaseReference.child(USER_METADATA).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String,String> userData = new HashMap<>();
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (DataHolder.currentUser == null)
                    DataHolder.currentUser = new User();
                if (currentUser != null) {
                    userData.put("name", currentUser.getDisplayName());
                    DataHolder.currentUser.setName(currentUser.getDisplayName());
                    userData.put("email", currentUser.getEmail());
                    DataHolder.currentUser.setEmail(currentUser.getEmail());
                    userData.put("photo", currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "");
                    DataHolder.currentUser.setPhotoURL(currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "");
                }
                if (location != null) {
                    userData.put("latitude", String.valueOf(location.getLatitude()));
                    userData.put("longitude", String.valueOf(location.getLongitude()));
                    DataHolder.currentUser.setUserLocation(new LatLng(location.getLatitude(),location.getLongitude()));
                }
                //TODO add phone number and Address here too
                userDatabaseReference.child(USER_METADATA).setValue(userData);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
}
