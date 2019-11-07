package com.firedrone.autoflydrone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dji.common.flightcontroller.FlightControllerState;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointMission;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionHeadingMode;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.products.Aircraft;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private float altitude = 100.0f;
    private float mSpeed = 10.0f;

    private List<Waypoint> waypointList = new ArrayList<>();
    private final Map<Integer, Marker> mMarkers = new ConcurrentHashMap<Integer, Marker>();

    private double droneLocationLat = 181, droneLocationLng = 181;
    private Marker droneMarker = null;
    private FlightController mFlightController;

    protected static final String TAG = "MapActivity";
    private GoogleMap gMap;

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    /**
     * @Description : RETURN BTN RESPONSE FUNCTION
     */
    public void onReturn(View view){
        Log.d(TAG, "onReturn");
        this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_panel);

        //Register BroadcastReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(DemoApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ImageButton locateButton = findViewById(R.id.locateButton);
        locateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDroneLocation();
                cameraUpdate();
            }
        });

    }
    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            onProductConnectionChange();
        }
    };

    private void onProductConnectionChange()
    {
        initFlightController();
    }

    private void initFlightController() {

        BaseProduct product = DemoApplication.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                mFlightController = ((Aircraft) product).getFlightController();
            }
        }

        if (mFlightController != null) {
            mFlightController.setStateCallback(new FlightControllerState.Callback() {

                @Override
                public void onUpdate(FlightControllerState djiFlightControllerCurrentState) {
                    droneLocationLat = djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
                    droneLocationLng = djiFlightControllerCurrentState.getAircraftLocation().getLongitude();
                    updateDroneLocation();
                }
            });
        }
    }

    public static boolean checkGpsCoordinates(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
    }

    private void updateDroneLocation(){

        LatLng pos = new LatLng(droneLocationLat, droneLocationLng);
        //Create MarkerOptions object
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(pos);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (droneMarker != null) {
                    droneMarker.remove();
                }

                if (checkGpsCoordinates(droneLocationLat, droneLocationLng)) {
                    droneMarker = gMap.addMarker(markerOptions);
                }
            }
        });
    }

    private void cameraUpdate(){
        LatLng pos = new LatLng(droneLocationLat, droneLocationLng);
        float zoomlevel = (float) 18.0;
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(pos, zoomlevel);
        gMap.moveCamera(cu);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
            gMap = googleMap;
            setUpMap();
    }

    private void setUpMap() {
        gMap.setOnMapClickListener(this);// add the listener for click for amap object
    }

    @Override
    public void onMapClick(LatLng latLng) {
        String name =  latLng.toString();
        gMap.addMarker(new MarkerOptions().position(latLng).title(name));
        gMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        markWaypoint(latLng);
    }

    private void markWaypoint(LatLng point){
        //Create MarkerOptions object
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        Marker marker = gMap.addMarker(markerOptions);
        mMarkers.put(mMarkers.size(), marker);
    }
}