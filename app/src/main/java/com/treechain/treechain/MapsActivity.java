package com.treechain.treechain;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Logger;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap = null;
    public static RequestQueue queue;
    private String ServerURL = "http://10.0.2.2:5000/api/v1/projects";
    private Logger logger = Logger.getLogger(MapsActivity.class.getName());
    private JSONObject projects = null;
    private Boolean initialized = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, ServerURL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                logger.info(response.toString());
                projects = response;
                initializeMarkers();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                logger.warning(error.getLocalizedMessage());
            }
        });

         queue = Volley.newRequestQueue(this);

         queue.add(jsonObjectRequest);

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
//        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMinZoomPreference(12);
//        mMap.setMaxZoomPreference(6);

        logger.info("Map is ready");

        initializeMarkers();

    }

    private void initializeMarkers()  {
        BitmapDescriptor mapIcon =  BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA);
        if (mMap == null || projects == null || initialized) return;

        JSONArray projs = null;
        try {
            projs = (JSONArray) projects.get("projects");
        } catch (JSONException e) {
            logger.warning(e.getLocalizedMessage());
            return;
        }

        for (int i = 0; i < projs.length(); i++) {
            try {
                JSONObject proj = (JSONObject) projs.get(i);

                double x = (int) proj.get("x");
                double y = (int) proj.get("y");
                final int id = (int) proj.get("id");
                String title = (String) proj.get("title");

                // Toledo Location
                x = 39.855785; y = -4.017002;
                title = "Project 1";

                LatLng projectMarker = new LatLng(x, y);
                mMap.addMarker(new MarkerOptions().position(projectMarker).title(title).icon(mapIcon)).showInfoWindow();
                mMap.moveCamera(CameraUpdateFactory.newLatLng(projectMarker));

                mMap.addMarker(new MarkerOptions().position(new LatLng( 39.871503, -4.047204 )).title("Project 2").icon(mapIcon));

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        startNewActivity(id);
                        return true;
                    }
                });
            } catch (JSONException e) {
                logger.warning(e.getLocalizedMessage());
                return;
            }
        }

        logger.info("Initialized markers");
        initialized = true;
    }


    public void startNewActivity(int id) {
        Intent intent = new Intent(this, TreeProjectActivity.class);
        intent.putExtra("PROJECT_ID", id);
        startActivity(intent);

    }

}
