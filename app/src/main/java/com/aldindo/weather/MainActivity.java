package com.aldindo.weather;

import android.Manifest;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private EditText search;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private ImageButton searchBTN;
    private TextView currentLocation, conditionText, tempInC, feelsLike;
    private String baseurl;
    Double lat, longitude;
    private RequestQueue requestQueue;

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        baseurl = "http://api.apixu.com/v1/forecast.json?key=ddff974835404ff7a96200457191408&q=";
        findViewMethods();
        fusedLocationProviderClient = new FusedLocationProviderClient(this);
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(100);
        locationRequest.setInterval(100);
        callPermission();
        requestLocation();


        //Log.d("test MeGga", "http://api.apixu.com/v1/forecast.json?key=ffcd12de6e62411fa5a150244191408&q=" + lat + longitude + "&days=" + 3);
        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();

        searchBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = baseurl + search.getText().toString() + "&days=" + 3;
                search.onEditorAction(EditorInfo.IME_ACTION_DONE);
                getWeather(url);
            }
        });

    }

    public void getWeather(String url) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject location = response.getJSONObject("location");
                    String cityName = location.getString("name");
                    currentLocation.setText(cityName);
                    JSONObject current = response.getJSONObject("current");
                    String temp_c = current.getString("temp_c");
                    JSONObject condition = current.getJSONObject("condition");
                    String conditionNow = condition.getString("text");
                    String feelsLIke = current.getString("feelslike_c");
                    feelsLike.setText("Feels Like " + feelsLIke + "°");

                    tempInC.setText(temp_c + "°");
                    conditionText.setText(conditionNow);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        requestQueue.add(jsonObjectRequest);
    }

    public void findViewMethods() {
        search = findViewById(R.id.search_bar_editText);
        searchBTN = findViewById(R.id.searchBTN);
        currentLocation = findViewById(R.id.currentLocation);
        conditionText = findViewById(R.id.condition);
        tempInC = findViewById(R.id.tempinC);
        feelsLike = findViewById(R.id.feelsLike);
    }

    public void callPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        Permissions.check(this/*context*/, permissions, null/*rationale*/, null/*options*/, new PermissionHandler() {
            @Override
            public void onGranted() {
                requestLocation();
            }

        });
    }

    public void requestLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PermissionChecker.PERMISSION_GRANTED)
        ) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    lat = locationResult.getLastLocation().getLatitude();
                    longitude = locationResult.getLastLocation().getLongitude();
                    //Log.d("latitude" + lat, " " + longitude);

                }

                @Override
                public void onLocationAvailability(LocationAvailability locationAvailability) {
                    super.onLocationAvailability(locationAvailability);
//                    getWeather("http://api.apixu.com/v1/forecast.json?key=ddff974835404ff7a96200457191408&q=" + lat +","+ longitude + "&days=" + 3);
//                    Log.d("test","http://api.apixu.com/v1/forecast.json?key=ddff974835404ff7a96200457191408&q=" + lat +","+ longitude + "&days=" + 3);
                }
            }, getMainLooper());
        } else {
            callPermission();
        }
    }


}
