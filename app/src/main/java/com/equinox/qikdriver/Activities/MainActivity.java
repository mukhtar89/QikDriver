package com.equinox.qikdriver.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.equinox.qikdriver.Adapters.NearbyPlacesRecyclerAdapter;
import com.equinox.qikdriver.Enums.QikList;
import com.equinox.qikdriver.Enums.Vehicle;
import com.equinox.qikdriver.Models.DataHolder;
import com.equinox.qikdriver.Models.Place;
import com.equinox.qikdriver.R;
import com.equinox.qikdriver.Utils.FusedLocationService;
import com.equinox.qikdriver.Utils.GetGooglePlaces;
import com.equinox.qikdriver.Utils.LocationPermission;
import com.equinox.qikdriver.Utils.MapUtils.SphericalUtil;
import com.equinox.qikdriver.Utils.StringManipulation;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Button loginButton;
    private TextView loginName, loginEmail, nearbyPlacesCount;
    private NetworkImageView loginImage;
    private FrameLayout profileFrame;
    private LocationPermission locationPermission;
    private GoogleMap mMap;
    private BottomSheetBehavior vicinityOrderDetails;
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationService fusedLocationService;
    private GetGooglePlaces getGooglePlaces;
    private List<Place> nearbyPlaces = new ArrayList<>();
    private ProgressDialog progressDialog;
    private Spinner vehicleType, shopType, showRange;
    private RecyclerView nearByPlacesList;
    private NearbyPlacesRecyclerAdapter nearbyPlacesRecyclerAdapter;
    private FloatingActionButton anchorFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    Log.d("AUTH", "OnAuthState: signed out");
                    loginButton.setVisibility(View.VISIBLE);
                    profileFrame.setVisibility(View.GONE);
                    loginEmail.setVisibility(View.GONE);
                    loginName.setVisibility(View.GONE);
                } else {
                    Log.d("AUTH", "OnAuthState: Signed in " + user.getUid());
                    DataHolder.userDatabaseReference =
                            DataHolder.database.getReference(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    DataHolder.getInstance().setRole("driver");
                    DataHolder.generateMetadata();
                    loginButton.setVisibility(View.GONE);
                    loginEmail.setVisibility(View.VISIBLE);
                    loginName.setVisibility(View.VISIBLE);
                    loginName.setText(user.getDisplayName());
                    loginEmail.setText(user.getEmail());
                    if (user.getPhotoUrl() != null){
                        profileFrame.setVisibility(View.VISIBLE);
                        loginImage.setImageUrl(user.getPhotoUrl().toString(), DataHolder.getInstance().getImageLoader());
                    }
                }
            }
        };

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View navigationHeaderView = navigationView.getHeaderView(0);
        profileFrame =(FrameLayout) navigationHeaderView.findViewById(R.id.profile_bundle);
        loginButton = (Button) navigationHeaderView.findViewById(R.id.login_button);
        loginName = (TextView) navigationHeaderView.findViewById(R.id.login_name);
        loginEmail = (TextView) navigationHeaderView.findViewById(R.id.login_email);
        loginImage = (NetworkImageView) navigationHeaderView.findViewById(R.id.login_image);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });
        loginImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
            }
        });

        vehicleType = (Spinner) findViewById(R.id.vehicle_type);
        shopType = (Spinner) findViewById(R.id.shop_type);
        showRange = (Spinner) findViewById(R.id.show_range);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, Vehicle.getListNames());
        ArrayAdapter<String> shopTypeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, QikList.getListTypeNames());
        ArrayAdapter<Integer> showRangeAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, Vehicle.getListRanges());
        vehicleType.setAdapter(categoryAdapter);
        shopType.setAdapter(shopTypeAdapter);
        showRange.setAdapter(showRangeAdapter);
        vehicleType.setSelection(4);
        shopType.setSelection(1);
        showRange.setSelection(0);
        vehicleType.setOnItemSelectedListener(filterItemListener);
        vehicleType.setOnItemSelectedListener(filterItemListener);
        vehicleType.setOnItemSelectedListener(filterItemListener);

        nearbyPlacesCount = (TextView) findViewById(R.id.neaby_places_count);
        View bottomSheet = findViewById(R.id.vicinity_bottom_sheet);
        vicinityOrderDetails = BottomSheetBehavior.from(bottomSheet);
        vicinityOrderDetails.setPeekHeight(300);
        vicinityOrderDetails.setState(BottomSheetBehavior.STATE_COLLAPSED);

        anchorFab = (FloatingActionButton) findViewById(R.id.fab2);
        nearByPlacesList = (RecyclerView) findViewById(R.id.nearby_places_list);
        nearByPlacesList.setLayoutManager(new LinearLayoutManager(this));
        nearByPlacesList.setHasFixedSize(true);
        nearbyPlacesRecyclerAdapter = new NearbyPlacesRecyclerAdapter(nearbyPlaces, nearbyPlacesCount, this);
        nearByPlacesList.setAdapter(nearbyPlacesRecyclerAdapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        locationPermission = new LocationPermission(this, this);
        fusedLocationService = new FusedLocationService(this, locationPermission, locationChangedListener);
        mGoogleApiClient = fusedLocationService.buildGoogleApiClient();
        getGooglePlaces = new GetGooglePlaces(progressDialog, placeDetailsFetchHandler);
    }

    private Handler placeDetailsFetchHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            nearbyPlaces.clear();
            nearbyPlaces.addAll(getGooglePlaces.returnPlaceList());
            nearbyPlacesRecyclerAdapter.notifyDataSetChanged();
            mMap.clear();
            for (final Place place : nearbyPlaces) {
                MarkerOptions markerAoptions = new MarkerOptions();
                markerAoptions.position(place.getLocation())
                        .title(StringManipulation.CapsFirst(place.getName()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_shopping_cart_black_24dp));
                Marker markerA = mMap.addMarker(markerAoptions);
                markerA.showInfoWindow();
            }
            return false;
        }
    });

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null)
            mAuth.removeAuthStateListener(mAuthListener);
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            SupportMapFragment supportMapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps_main));
            supportMapFragment.getMapAsync(this);
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        if (locationPermission.checkLocationPermission())
            mMap.setMyLocationEnabled(true);
        else locationPermission.getLocationPermission();
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setUpMap();
                    Toast.makeText(MainActivity.this, "Location is Set!", Toast.LENGTH_LONG).show();
                } else {
                    locationPermission = new LocationPermission(this, this);
                    locationPermission.getLocationPermission();
                    Toast.makeText(MainActivity.this, "Location Access is Denied!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setUpMap();
        mMap.setPadding(0,200,40,0);
    }

    private Handler locationChangedListener = new Handler(new Handler.Callback() {
        LatLng oldLocation = null;
        @Override
        public boolean handleMessage(Message msg) {
            if (fusedLocationService.returnLocation() != null) {
                if (oldLocation == null) {
                    oldLocation = fusedLocationService.returnLocation();
                    fetchOrders(oldLocation);
                } else if (SphericalUtil.computeDistanceBetween(oldLocation, fusedLocationService.returnLocation())
                        > showRange.getSelectedItemPosition()*1000) {
                    fetchOrders(oldLocation);
                    oldLocation = fusedLocationService.returnLocation();
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(fusedLocationService.returnLocation(), 14));
            }
            return false;
        }
    });

    private void fetchOrders(final LatLng location) {
        Vehicle vehicle = vehicleType.getSelectedItemPosition() == 0 ? null : Vehicle.values()[vehicleType.getSelectedItemPosition()-1];
        QikList qikList = shopType.getSelectedItemPosition() == 0 ? null : QikList.values()[shopType.getSelectedItemPosition()-1];
        Integer range = Vehicle.getListRanges().get(showRange.getSelectedItemPosition());
        getGooglePlaces.parsePlaces(location, vehicle, qikList, range);
    }

    private AdapterView.OnItemSelectedListener filterItemListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    /*@Override
    public void onClick(View v) {
        vicinityOrderDetails.setState(BottomSheetBehavior.STATE_EXPANDED);
        if (vicinityOrderDetails.getState() == BottomSheetBehavior.STATE_EXPANDED)
            anchorFab.setVisibility(View.GONE);
        else anchorFab.setVisibility(View.VISIBLE);
    }*/
}
