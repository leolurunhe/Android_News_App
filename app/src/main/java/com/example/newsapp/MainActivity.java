package com.example.newsapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.newsapp.ui.home.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements LocationListener {

    protected Context context = this;
    private AutoSuggestAdapter mAutoSuggestAdapter;
    private final int REQUEST_LOCATION_PERMISSION = 1;
    private RequestQueue queue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        queue = Volley.newRequestQueue(this);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_headlines, R.id.navigation_trending, R.id.navigation_bookmarks)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

    }

    @SuppressLint("ResourceType")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_search, menu);
        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView =(SearchView) searchItem.getActionView();

        final SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);

        searchAutoComplete.setHintTextColor(Color.GRAY);
        searchAutoComplete.setThreshold(3);
        searchAutoComplete.setDropDownBackgroundResource(R.color.white);
        searchAutoComplete.setTextColor(Color.BLACK);

        mAutoSuggestAdapter = new AutoSuggestAdapter(this, android.R.layout.simple_dropdown_item_1line);
        searchAutoComplete.setAdapter(mAutoSuggestAdapter);
        searchAutoComplete.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String queryString =(String)parent.getItemAtPosition(position);
                        searchAutoComplete.setText(queryString);
                    }
                }
        );
        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener(){
                    @Override
                    public boolean onQueryTextChange(String text) {
                        if (text.length() > 0){
                            String url = getString(R.string.autoSuggestUrl);
                            getHint(text, searchAutoComplete, url);
                        }
                        return false;
                    }

                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        if (query.length() > 0) {
                            //TODO go to search activity
                            Intent intent = new Intent(context, SearchActivity.class);
                            intent.putExtra("keyword", query);
                            startActivity(intent);
                        }
                        return false;
                    }
                });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onStart() {
        super.onStart();
        requestLocationPermission();

    }

    public void getHint(String text, final SearchView.SearchAutoComplete searchAutoComplete, String url){
        url += text;

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,url,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    List<String> suggests = new ArrayList<>();
                    JSONArray array = response.getJSONArray("suggestionGroups");
                    if(array.length() > 0){
                        JSONArray suggestions = array.getJSONObject(0).getJSONArray("searchSuggestions");
                        for(int i=0; i<suggestions.length(); ++i){
                            String temp = suggestions.getJSONObject(i).getString("displayText");
                            suggests.add(temp);
                        }
                    }
                    mAutoSuggestAdapter.setData(suggests);
                    mAutoSuggestAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Autocomplete is not working " + error);
            }
        }) {

            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Ocp-Apim-Subscription-Key", getString(R.string.bingAutoSuggestKey));
                return headers;
            }
        };
        queue.add(req);
    }

    @Override
    public void onRestart() {
        super.onRestart();
        //When BACK BUTTON is pressed, the activity on the stack is restarted
        //Do what you want on the refresh procedure here

    }

    @Override
    public void onResume() {
        super.onResume();
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms) && this.findViewById(R.id.City) != null) {

            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    public void requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if(EasyPermissions.hasPermissions(this, perms)) {
            //Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
        else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
        }
    }


    @Override
    public void onAttachFragment(Fragment fragment){
        if (fragment instanceof HomeFragment) {
            HomeFragment homeFragment = (HomeFragment) fragment;
            //homeFragment.setOnHomeFragmentInteractionListener(this);
        }

    }


    // Listener for clicking item in home recycler view.



    private final String FAV_FILE = "favorite_news";






    @Override
    public void onLocationChanged(Location location) {
        TextView state = findViewById(R.id.State);
        if(state != null){
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                String cityName = "";
                cityName = addresses.get(0).getLocality();
                String stateName = addresses.get(0).getAdminArea();
                TextView cityView = this.findViewById(R.id.City);
                cityView.setText(cityName);
                state.setText(stateName);
                if(!cityName.equals("")) {
                    String url = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&units=metric&appid=12b40c52e31077255f06c21d57bdfad0";
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onResponse(JSONObject response) {
                            // Display the first 500 characters of the response string.
                            //textView.setText("Response is: "+ response.substring(0,500));
                            try {
                                double temp1 = response.getJSONObject("main").getDouble("temp");
                                int temperature = (int)temp1;
                                String weather = response.getJSONArray("weather").getJSONObject(0).getString("main");
                                TextView tempView = findViewById(R.id.Temperature);
                                TextView weatherView = findViewById(R.id.Weather);
                                tempView.setText(temperature + "\u2103");
                                weatherView.setText(weather);
                                LinearLayout a = findViewById(R.id.weather_img);
                                switch (weather){
                                    case "Clear":
                                        a.setBackground(getDrawable(R.drawable.clear_weather));
                                        break;
                                    case "Clouds":
                                        a.setBackground(getDrawable(R.drawable.cloudy_weather));
                                        break;
                                    case "Snow":
                                        a.setBackground(getDrawable(R.drawable.snowy_weather));
                                        break;
                                    case "Rain":
                                    case "Drizzle":
                                        a.setBackground(getDrawable(R.drawable.rainy_weather));
                                        break;
                                    case "Thunderstorm":
                                        a.setBackground(getDrawable(R.drawable.thunder_weather));
                                        break;
                                    default:
                                        a.setBackground(getDrawable(R.drawable.sunny_weather));
                                }


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            System.out.println("This is not working " + error);
                        }
                    });

                    // Add the request to the RequestQueue.
                    queue.add(jsonObjectRequest);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

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
}
