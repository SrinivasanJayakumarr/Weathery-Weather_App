package com.example.weathery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity{

    public static final String TAG = "Main_weather";

    private ArrayList<ForecastModel> forecastModelArrayList;
    private CyclerViewAdapter cyclerViewAdapter;
    FusedLocationProviderClient fusedLocationProviderClient; //For getting current location

    private TextView cityName;
    private TextView weatherStatus;
    private TextView currentTempValue;
    private TextView currentWindValue;
    private TextView currentHumidityValue;
    private TextView currentPressureValue;
    private TextView currentPollutionValue;
    private TextView currentSo2level;
    private TextView currentNo2level;
    private ImageView currentWeatherIcon;
    private TextView sublocation;

    private RecyclerView recyclerView;

    String iconName = ""; //For forecastweatherIcon method : updateForecastWeather
    int forecastTemp;
    long forecastTime;
    String forecastTimeStr = "";
    String forecastTempStr = "";

    String myCity = "";
    double longitude;//= 78.6988;
    double lattitude;//= 10.8029;// For search use

    public SharedPreferences prefOTEO; // For one time execution
    public SharedPreferences.Editor editorO;
    boolean okOrNot = true;

    public String sublocationsearch = "";
    public String sublocationPref = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG,"onCreate fired.");

        //fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        /*if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            getLocation();
        }else {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
        }*/

        final Intent intent = getIntent();
        okOrNot = intent.getBooleanExtra("BooleanData",true);
        Log.d(TAG,"okOrNot Value : "+okOrNot);
        String mCity = intent.getStringExtra("mCity");
        if (mCity != null) {
            getSearchResult(mCity);
        }

        if (okOrNot){

            Log.d(TAG,"Inside okOrNot");

            prefOTEO = PreferenceManager.getDefaultSharedPreferences(this);

            editorO = prefOTEO.edit();
            editorO.putBoolean("NewOnce",false);
            editorO.apply();

            if (!prefOTEO.getBoolean("NewOnce",false)){

                SharedPreferences sh = getSharedPreferences("location", MODE_PRIVATE);
                myCity = sh.getString("locationName", "");
                longitude = sh.getFloat("locationLong", 0); //Problem here
                lattitude = sh.getFloat("locationLat", 0);

                updateLocationWeather(longitude,lattitude);
                updatePollution(longitude,lattitude);
                updateForecastWeather(longitude,lattitude);
                sublocationPref = myCity;

                //sublocation.setText(myCity);

                editorO = prefOTEO.edit();
                editorO.putBoolean("NewOnce",true);
                editorO.apply();

            }

        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this); // locationrequest

        recyclerView = findViewById(R.id.rcv_today_weather_list);
        RelativeLayout relativeLayout = findViewById(R.id.search_view_btn);
        RelativeLayout locationLayout = findViewById(R.id.location_btn); // For getting current location weather

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(intent);
            }
        });

        locationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });

        forecastModelArrayList = new ArrayList<>();
        //forecastModelArrayList.add(new ForecastModel("99","11d","12.00 AM"));

        //Initializing adapter for RecyclerView and Pass Arraylist to ForecastModel
        cyclerViewAdapter = new CyclerViewAdapter(this, forecastModelArrayList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(cyclerViewAdapter);


        cityName = findViewById(R.id.city_name);
        weatherStatus = findViewById(R.id.weather_status);  //ok (description)
        currentTempValue = findViewById(R.id.current_temp_value);   //ok
        currentHumidityValue = findViewById(R.id.humidity_value);   //ok
        currentWindValue = findViewById(R.id.wind_value);   //ok
        currentPressureValue = findViewById(R.id.pressure_value);   //ok
        currentPollutionValue = findViewById(R.id.pollution_value);
        currentNo2level = findViewById(R.id.no2_value);
        currentSo2level = findViewById(R.id.so2_value);
        sublocation = findViewById(R.id.sublocation_txt);

        currentWeatherIcon = findViewById(R.id.current_weather_icon);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        editorO = prefOTEO.edit();
        editorO.putBoolean("NewOnce",false);
        editorO.apply();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        finish();
    }

    private void getSearchResult(String requiredCity) {

        if (requiredCity != null) {
            Toast.makeText(this, requiredCity, Toast.LENGTH_SHORT).show();
            if (Geocoder.isPresent()) {
                try {
                    //String requiredCity = mCity;
                    Geocoder geocoder = new Geocoder(this);
                    List<Address> requiredAddress = geocoder.getFromLocationName(requiredCity, 5);

                    List<Double> ll = new ArrayList<>(requiredAddress.size());
                    for (Address a : requiredAddress) {
                        if (a.hasLatitude() && a.hasLongitude()) {
                            ll.add(a.getLatitude());
                            ll.add(a.getLongitude());
                            //Toast.makeText(this, "cLat" + ll.get(0) + "cLon" + ll.get(1), Toast.LENGTH_SHORT).show();
                        }
                    }

                    double searchLat = ll.get(0);
                    double searchLon = ll.get(1);

                    //Typecasting
                    float num1 = (float) searchLat;
                    float num2 = (float) searchLon;

                    SharedPreferences sharedPreferences = getSharedPreferences("location", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("locationName", requiredCity);
                    editor.putFloat("locationLong", num2);
                    editor.putFloat("locationLat", num1);
                    editor.apply();

                    sublocationsearch = requiredCity;
                    updateLocationWeather(searchLon,searchLat);
                    updatePollution(searchLon, searchLat);
                    updateForecastWeather(searchLon, searchLat);

                    /*if (forecastModelArrayList != null && forecastModelArrayList.size()>0) {
                        forecastModelArrayList.clear();
                        cyclerViewAdapter.notifyDataSetChanged();
                    }*/
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

    }

    public void displayLocationSettingsRequest(Context context) {

        //This method is used to enable location by popping up location permission dialog
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        getLocation();
                        Log.d(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.d(TAG, "Location settings are not satisfied.");
                        try {
                            status.startResolutionForResult(MainActivity.this, 0x1);
                            Toast.makeText(getApplicationContext(), "Plz..Try again Once..", Toast.LENGTH_SHORT).show();
                        } catch (IntentSender.SendIntentException e) {
                            Log.d(TAG, "PendingIntent Unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.d(TAG, "Location settings are inadequate,and cant be fixed here.Dialog not created.");
                        break;

                }
            }
        });

    }

    private void getLocation() {

        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //getLocation();

            LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            boolean gps_enabled;//To check if gps is turned on or not
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);//To check if location settings are enabled or not

            Log.d(TAG, "Before request...");
            //fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            Log.d(TAG, "After request...");
            Log.d(TAG, "GPS Enabled" + gps_enabled);

            if (!gps_enabled) {
                displayLocationSettingsRequest(this);
            } else {

                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        Location location = task.getResult();
                        Log.d(TAG, "Location : " + location);

                        if (location != null) {
                            try {
                                Geocoder geocoder = new Geocoder(MainActivity.this,
                                        Locale.getDefault());
                                List<Address> addresses = geocoder.getFromLocation(
                                        location.getLatitude(), location.getLongitude(), 1);

                                //Set longitude and lattitude
                                lattitude = addresses.get(0).getLatitude();
                                Log.d(TAG, "Lattitude : " + lattitude);
                                longitude = addresses.get(0).getLongitude();
                                Log.d(TAG, "Longitude : " + longitude);
                                myCity = addresses.get(0).getLocality();
                                String wantedCity = addresses.get(0).getLocality();
                                Log.d(TAG, "City : " + myCity);
                                //Toast.makeText(MainActivity.this,"City : "+myCity , Toast.LENGTH_SHORT).show();

                                //To clear the arraylist to prevent data duplication
                                if (forecastModelArrayList != null && forecastModelArrayList.size() > 0) {
                                    forecastModelArrayList.clear();
                                }

                                //Call methods
                                sublocationsearch = wantedCity;
                                updateLocationWeather(longitude,lattitude);//updateWeather(myCity);
                                updatePollution(longitude, lattitude);
                                updateForecastWeather(longitude, lattitude);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            //getLastKnownLocation();
                            Log.d(TAG,"Nothing happened.");
                            Toast.makeText(MainActivity.this, "Try search...Can't get location right now.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

        }else {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
        }

    }


    public void updateLocationWeather(double longitude, double lattitude) {

        String url = "https://api.openweathermap.org/data/2.5/weather?lat="+lattitude+"&lon="+longitude+"&appid=6091d7651a1cc6ab4b6e742d900dfb3d&units=metric";
        //String url = "https://api.openweathermap.org/data/2.5/air_pollution?lat="+lattitude+"&lon="+longitude+"&appid=6091d7651a1cc6ab4b6e742d900dfb3d";

        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                //Log.d(TAG,"Before response value");
                Log.d(TAG,"Weather : "+response);

                try {
                    JSONObject main_object = response.getJSONObject("main");
                    JSONArray array = response.getJSONArray("weather");
                    JSONObject object = array.getJSONObject(0);
                    JSONObject wind_object = response.getJSONObject("wind");

                    int temp = main_object.getInt("temp");
                    int humi = main_object.getInt("humidity");
                    int pressu = main_object.getInt("pressure");
                    String city_name = response.getString("name");
                    String status = object.getString("description");
                    Double speed = wind_object.getDouble("speed");

                    //int temp_int = Integer.parseInt(temp);

                    currentTempValue.setText(String.valueOf(temp));
                    currentHumidityValue.setText(String.valueOf(humi));
                    currentPressureValue.setText(String.valueOf(pressu));
                    currentWindValue.setText(String.valueOf(speed));
                    String upperCaseStatus = status.substring(0,1).toUpperCase()+status.substring(1).toLowerCase();
                    weatherStatus.setText(upperCaseStatus);
                    String upperCaseCity = city_name.substring(0,1).toUpperCase()+city_name.substring(1).toLowerCase();
                    sublocation.setText(upperCaseCity);
                    //cityName.setText(upperCaseCity);
                    if (okOrNot){
                        cityName.setText(sublocationPref);
                    }else {
                        cityName.setText(sublocationsearch);
                    }

                    String[] clear = {"clear sky"};
                    String[] clouds = {"few clouds","scattered clouds","broken clouds","overcast clouds"};
                    String[] atmo = {"mist","smoke","haze","sand/dust whirls","fog","sand","dust","volcanic ash","squalls","tornado"};
                    String[] snow = {"light snow","snow","heavy snow","sleet","light shower sleet","shower sleet","light rain and snow","rain and snow","light shower snow","shower snow","heavy shower snow"};
                    String[] rain = {"shower rain","light rain","moderate rain","heavy intensity rain","very heavy rain","extreme rain","freezing rain","light intensity shower rain","heavy intensity shower rain","ragged shower rain"};
                    String[] drizzle = {"drizzle","light intensity drizzle","heavy intensity drizzle","light intensity drizzle rain","drizzle rain","heavy intensity drizzle rain","shower rain and drizzle","heavy shower rain and drizzle","shower drizzle"};
                    String[] thunder = {"thunderstorm","thunderstorm with light rain","thunderstorm with rain","thunderstorm with heavy rain","light thunderstorm","heavy thunderstorm","ragged thunderstorm","thunderstorm with light drizzle","thunderstorm with drizzle","thunderstorm with heavy drizzle"};

                    //To check the condition

                    List one = Arrays.asList(clouds);
                    List two = Arrays.asList(atmo);
                    List three = Arrays.asList(snow);
                    List four = Arrays.asList(rain);
                    List five = Arrays.asList(drizzle);
                    List six = Arrays.asList(thunder);
                    List seven = Arrays.asList(clear);

                    String iconUrl = "";

                    if (one.contains(status)){

                        iconUrl = "https://openweathermap.org/img/wn/02d@4x.png";
                        //Log.d(TAG,"Image dowmnloaded");
                        Picasso.get().load(iconUrl).into(currentWeatherIcon);
                    }
                    if (two.contains(status)){
                        iconUrl = "https://openweathermap.org/img/wn/50d@4x.png";
                        //Log.d(TAG,"Image dowmnloaded");
                        Picasso.get().load(iconUrl).into(currentWeatherIcon);
                    }
                    if (three.contains(status)){
                        iconUrl = "https://openweathermap.org/img/wn/13d@4x.png";
                        //Log.d(TAG,"Image dowmnloaded");
                        Picasso.get().load(iconUrl).into(currentWeatherIcon);
                    }
                    if (four.contains(status)){
                        iconUrl = "https://openweathermap.org/img/wn/10d@4x.png";
                        //Log.d(TAG,"Image dowmnloaded");
                        Picasso.get().load(iconUrl).into(currentWeatherIcon);
                    }
                    if (five.contains(status)){
                        iconUrl = "https://openweathermap.org/img/wn/09d@4x.png";
                        //Log.d(TAG,"Image dowmnloaded");
                        Picasso.get().load(iconUrl).into(currentWeatherIcon);
                    }
                    if (six.contains(status)){
                        iconUrl = "https://openweathermap.org/img/wn/11d@4x.png";
                        //Log.d(TAG,"Image dowmnloaded");
                        Picasso.get().load(iconUrl).into(currentWeatherIcon);
                    }
                    if (seven.contains(status)){
                        iconUrl = "https://openweathermap.org/img/wn/01d@4x.png";
                        //Log.d(TAG,"Image dowmnloaded");
                        Picasso.get().load(iconUrl).into(currentWeatherIcon);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                error.printStackTrace();

            }
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jor);
    }


    public void updateWeather(String myCity) {

        String url = "https://api.openweathermap.org/data/2.5/weather?q="+myCity+",in&appid=6091d7651a1cc6ab4b6e742d900dfb3d&units=metric";

        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                //Log.d(TAG,"Before response value");
                Log.d(TAG,"Weather : "+response);

                try {
                    JSONObject main_object = response.getJSONObject("main");
                    JSONArray array = response.getJSONArray("weather");
                    JSONObject object = array.getJSONObject(0);
                    JSONObject wind_object = response.getJSONObject("wind");

                    int temp = main_object.getInt("temp");
                    int humi = main_object.getInt("humidity");
                    int pressu = main_object.getInt("pressure");
                    String city_name = response.getString("name");
                    String status = object.getString("description");
                    Double speed = wind_object.getDouble("speed");

                    //int temp_int = Integer.parseInt(temp);

                    currentTempValue.setText(String.valueOf(temp));
                    currentHumidityValue.setText(String.valueOf(humi));
                    currentPressureValue.setText(String.valueOf(pressu));
                    currentWindValue.setText(String.valueOf(speed));
                    String upperCaseStatus = status.substring(0,1).toUpperCase()+status.substring(1).toLowerCase();
                    weatherStatus.setText(upperCaseStatus);
                    String upperCaseCity = city_name.substring(0,1).toUpperCase()+city_name.substring(1).toLowerCase();
                    cityName.setText(upperCaseCity);

                    String[] clear = {"clear sky"};
                    String[] clouds = {"few clouds","scattered clouds","broken clouds","overcast clouds"};
                    String[] atmo = {"mist","smoke","haze","sand/dust whirls","fog","sand","dust","volcanic ash","squalls","tornado"};
                    String[] snow = {"light snow","snow","heavy snow","sleet","light shower sleet","shower sleet","light rain and snow","rain and snow","light shower snow","shower snow","heavy shower snow"};
                    String[] rain = {"shower rain","light rain","moderate rain","heavy intensity rain","very heavy rain","extreme rain","freezing rain","light intensity shower rain","heavy intensity shower rain","ragged shower rain"};
                    String[] drizzle = {"drizzle","light intensity drizzle","heavy intensity drizzle","light intensity drizzle rain","drizzle rain","heavy intensity drizzle rain","shower rain and drizzle","heavy shower rain and drizzle","shower drizzle"};
                    String[] thunder = {"thunderstorm","thunderstorm with light rain","thunderstorm with rain","thunderstorm with heavy rain","light thunderstorm","heavy thunderstorm","ragged thunderstorm","thunderstorm with light drizzle","thunderstorm with drizzle","thunderstorm with heavy drizzle"};

                    //To check the condition

                    List one = Arrays.asList(clouds);
                    List two = Arrays.asList(atmo);
                    List three = Arrays.asList(snow);
                    List four = Arrays.asList(rain);
                    List five = Arrays.asList(drizzle);
                    List six = Arrays.asList(thunder);
                    List seven = Arrays.asList(clear);

                    String iconUrl = "";

                    if (one.contains(status)){

                        iconUrl = "https://openweathermap.org/img/wn/02d@4x.png";
                        //Log.d(TAG,"Image dowmnloaded");
                        Picasso.get().load(iconUrl).into(currentWeatherIcon);
                    }
                    if (two.contains(status)){
                        iconUrl = "https://openweathermap.org/img/wn/50d@4x.png";
                        //Log.d(TAG,"Image dowmnloaded");
                        Picasso.get().load(iconUrl).into(currentWeatherIcon);
                    }
                    if (three.contains(status)){
                        iconUrl = "https://openweathermap.org/img/wn/13d@4x.png";
                        //Log.d(TAG,"Image dowmnloaded");
                        Picasso.get().load(iconUrl).into(currentWeatherIcon);
                    }
                    if (four.contains(status)){
                        iconUrl = "https://openweathermap.org/img/wn/10d@4x.png";
                        //Log.d(TAG,"Image dowmnloaded");
                        Picasso.get().load(iconUrl).into(currentWeatherIcon);
                    }
                    if (five.contains(status)){
                        iconUrl = "https://openweathermap.org/img/wn/09d@4x.png";
                        //Log.d(TAG,"Image dowmnloaded");
                        Picasso.get().load(iconUrl).into(currentWeatherIcon);
                    }
                    if (six.contains(status)){
                        iconUrl = "https://openweathermap.org/img/wn/11d@4x.png";
                        //Log.d(TAG,"Image dowmnloaded");
                        Picasso.get().load(iconUrl).into(currentWeatherIcon);
                    }
                    if (seven.contains(status)){
                        iconUrl = "https://openweathermap.org/img/wn/01d@4x.png";
                        //Log.d(TAG,"Image dowmnloaded");
                        Picasso.get().load(iconUrl).into(currentWeatherIcon);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                error.printStackTrace();

            }
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jor);
    }


    public void updatePollution(double longitude, double lattitude) {

        //first value lattitude and longitude  Geo coords chennai [13.0878, 80.2785],trichy Geo coords [10.8029, 78.6988]
        //kovai Geo coords [11, 76.9667] madurai Geo coords [9.9333, 78.1167] tanjore Geo coords [10.8, 79.15]
        //String key = "6091d7651a1cc6ab4b6e742d900dfb3d";
        String url = "https://api.openweathermap.org/data/2.5/air_pollution?lat="+lattitude+"&lon="+longitude+"&appid=6091d7651a1cc6ab4b6e742d900dfb3d";
        //http://api.openweathermap.org/data/2.5/air_pollution?lat=11.1415&lon=78.5945&appid=6091d7651a1cc6ab4b6e742d900dfb3d

        //Log.d(TAG,"Before JSON request");

        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                //Log.d(TAG,"Pollution:");
                Log.d(TAG,"Pollution : "+response);

                try {
                    //JSONObject main_object = response.getJSONObject("main");
                    JSONArray array = response.getJSONArray("list");
                    JSONObject object = array.getJSONObject(0);
                    JSONObject airQuality_object = object.getJSONObject("main");
                    JSONObject components = object.getJSONObject("components");


                    int airQuality = airQuality_object.getInt("aqi");
                    double no2level = components.getDouble("no2");
                    double so2level = components.getDouble("so2");

                    currentNo2level.setText("NO2 : "+String.valueOf(no2level));
                    currentSo2level.setText("SO2 : "+String.valueOf(so2level));

                    //int temp_int = Integer.parseInt(temp);

                    switch (airQuality){
                        case 1: String value = "Good";
                            currentPollutionValue.setText(value);
                            //Log.d(TAG,"Case 1");
                            break;
                        case 2: value = "Fair";
                            currentPollutionValue.setText(value);
                            //Log.d(TAG,"Case 2");
                            break;
                        case 3: value = "Moderate";
                            currentPollutionValue.setText(value);
                            //Log.d(TAG,"Case 3");
                            break;
                        case 4: value = "Poor";
                            currentPollutionValue.setText(value);
                            //Log.d(TAG,"Case 4");
                            break;
                        case 5: value = "Very Poor";
                            currentPollutionValue.setText(value);
                            //Log.d(TAG,"Case 5");
                            break;
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                error.printStackTrace();

            }
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jor);
    }


    public void updateForecastWeather(double longitude, double lattitude) {

        //first value lattitude and longitude  Geo coords chennai [13.0878, 80.2785],trichy Geo coords [10.8029, 78.6988]
        //kovai Geo coords [11, 76.9667] madurai Geo coords [9.9333, 78.1167] tanjore Geo coords [10.8, 79.15]
        //String key = "6091d7651a1cc6ab4b6e742d900dfb3d";
        String url = "https://api.openweathermap.org/data/2.5/onecall?lat="+lattitude+"&lon="+longitude+"&exclude=minutely,daily&appid=6091d7651a1cc6ab4b6e742d900dfb3d&units=metric";
        //Log.d(TAG,"Before JSON request");

        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                //Log.d(TAG,"Forecast : ");
                Log.d(TAG,"Forecast : "+response);

                try {
                        for (int i=0; i<=11; i++) {
                            //JSONObject main_object = response.getJSONObject("main");
                            JSONArray array = response.getJSONArray("hourly");
                            JSONObject object = array.getJSONObject(i);
                            forecastTime = object.getLong("dt");
                            forecastTemp = object.getInt("temp");

                            JSONArray iconArray = object.getJSONArray("weather");
                            JSONObject iconArrayJSONObject = iconArray.getJSONObject(0);
                            iconName = iconArrayJSONObject.getString("icon");

                            //Typecasting
                            forecastTempStr = String.valueOf(forecastTemp);

                            Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                            calendar.setTimeInMillis(forecastTime * 1000);
                            forecastTimeStr = DateFormat.format("hh:mm a", calendar).toString();

                            forecastModelArrayList.add(new ForecastModel(forecastTempStr,iconName,forecastTimeStr));//Add data to arraylist

                        }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                error.printStackTrace();

            }
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jor);
    }

}