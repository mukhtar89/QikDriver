package com.equinox.qikdriver.Utils;

import android.app.Dialog;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.equinox.qikdriver.Enums.QikList;
import com.equinox.qikdriver.Enums.Vehicle;
import com.equinox.qikdriver.Models.Constants;
import com.equinox.qikdriver.Models.DataHolder;
import com.equinox.qikdriver.Models.Photo;
import com.equinox.qikdriver.Models.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static com.equinox.qikdriver.Enums.OrderStatus.CANCELLED;

/**
 * Created by mukht on 10/30/2016.
 */

public class GetGooglePlaces {

    private String TAG = GetGooglePlaces.class.getSimpleName();
    private String NORMAL = "1", SECONDARY = "2";
    private Dialog pDialog;
    private Handler placeHandler;
    private List<Place> placeList = new ArrayList<>();
    private HashSet<String> loadedPlaces;

    public GetGooglePlaces(Dialog pDialog, Handler placeHandler) {
        this.pDialog = pDialog;
        this.placeHandler = placeHandler;
        loadedPlaces = new HashSet<>();
    }

    public synchronized void parsePlaces(LatLng location, final Vehicle vehicleType, QikList placeType, Integer range) {
        placeList = new ArrayList<>();
        List<QikList> placeTypes = new ArrayList<>();
        if (placeType == null) placeTypes = Arrays.asList(QikList.values());
        else placeTypes.add(placeType);
        for (QikList qikList : placeTypes) {
            String baseURL = "https://maps.googleapis.com/maps/api/place/search/json?";
            String urlArguments = "location=" + location.latitude + "," + location.longitude + "&radius=" + range * 1000
                    + "&type=" + qikList.getTypeName() + "&sensor=true_or_false&key=" + Constants.PLACES_API_KEY;
            JsonObjectRequest placeReq = new JsonObjectRequest(baseURL + urlArguments, null, placesListener, placesErrorListener);
            AppVolleyController.getInstance().addToRequestQueue(placeReq, NORMAL);
            for (String keyword : Arrays.asList(qikList.getKeyword())) {
                baseURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
                urlArguments = "location=" + location.latitude + "," + location.longitude + "&radius=" + range * 1000
                        + "&keyword=" + keyword + "&sensor=true_or_false&key=" + Constants.PLACES_API_KEY;
                JsonObjectRequest placeReqSecondary = new JsonObjectRequest(baseURL + urlArguments, null, placesListener, placesErrorListener);
                AppVolleyController.getInstance().addToRequestQueue(placeReqSecondary, SECONDARY);
            }
        }
        AppVolleyController.getInstance().getRequestQueue().addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
            @Override
            public void onRequestFinished(Request<Object> request) {
                hidePDialog();
                filterPlacesByWeight(vehicleType);
                AppVolleyController.getInstance().getRequestQueue().removeRequestFinishedListener(this);
            }
        });
    }

    private synchronized void filterPlacesByWeight(final Vehicle vehicleType) {
        List<Place> tempPlaceList = placeList;
        for (final Place place : tempPlaceList) {
            final DatabaseReference businessOrderReference = DataHolder.database.getReference()
                    .child(place.getPlaceId()).child(Constants.ORDER);
            businessOrderReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        Iterator<DataSnapshot> iteratorIncoming = dataSnapshot.getChildren().iterator();
                        HashMap<String, Object> iteratorObject;
                        int redFlag = 0;
                        while (iteratorIncoming.hasNext()) {
                            DataSnapshot orderShot = iteratorIncoming.next();
                            iteratorObject = (HashMap<String, Object>) orderShot.getValue();
                            redFlag++;
                            if ((Long) iteratorObject.get("orderPayload") > (vehicleType != null ? vehicleType.getWeight() : 100)
                                    ||(iteratorObject.get("orderStatus")).equals(CANCELLED.getNodeName()))
                                redFlag--;
                        }
                        if (redFlag == 0){
                            placeList.remove(place);
                            placeHandler.sendMessageAtFrontOfQueue(new Message());
                        }
                    }
                    else {
                        placeList.remove(place);
                        placeHandler.sendMessageAtFrontOfQueue(new Message());
                    }
                    businessOrderReference.removeEventListener(this);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }
    }

    private Response.Listener<JSONObject> placesListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {
            Log.d(TAG, response.toString());
            try {
                if (response.has("results")) {
                    JSONArray listObjects = response.getJSONArray("results");
                    // Parsing json
                    for (int i = 0; i < listObjects.length(); i++) {
                        JSONObject obj = listObjects.getJSONObject(i);
                        Place place = new Place();
                        if (!loadedPlaces.contains(obj.getString("place_id"))){
                            JSONObject location = obj.getJSONObject("geometry").getJSONObject("location");
                            place.setLocation(new LatLng(location.getDouble("lat"), location.getDouble("lng")));
                            place.setIconURL(obj.getString("icon"));
                            place.setName(obj.getString("name"));
                            if (obj.has("opening_hours")) {
                                JSONObject openingHours = obj.getJSONObject("opening_hours");
                                place.setOpenNow(openingHours.getBoolean("open_now"));
                            }
                            JSONArray photos = obj.getJSONArray("photos");
                            JSONObject photo = photos.getJSONObject(0);
                            Photo tempPhoto = new Photo(photo.getInt("width"), photo.getInt("height"), null, photo.getString("photo_reference"));
                            List<Photo> photoList= new ArrayList<>();
                            photoList.add(tempPhoto);
                            place.setPhoto(photoList);
                            place.setPlaceId(obj.getString("place_id"));
                            place.setVicinity(obj.getString("vicinity"));
                            placeList.add(place);
                            loadedPlaces.add(obj.getString("place_id"));
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Response.ErrorListener placesErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            VolleyLog.d(TAG, "Error: " + error.getMessage());
            hidePDialog();
        }
    };

    private void hidePDialog() {
        if (pDialog != null)
            pDialog.dismiss();
    }

    public List<Place> returnPlaceList() {
        return placeList;
    }
}
