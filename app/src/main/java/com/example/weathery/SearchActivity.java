package com.example.weathery;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        SearchView searchView = findViewById(R.id.sv);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //getCity(query);
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                intent.putExtra("mCity",query);
                intent.putExtra("BooleanData",false);
                startActivity(intent);
                finish();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    /*private String getCity(String query) {

        String city_name = query;
        Toast.makeText(SearchActivity.this, "City Sent", Toast.LENGTH_SHORT).show();
        return city_name;

    }*/
}