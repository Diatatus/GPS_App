package com.example.gpstrack;

import android.location.Location;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class ShowSaveLocationList extends AppCompatActivity {

    ListView lv_savedLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_save_location_list);


            lv_savedLocations = findViewById(R.id.lv_wayPoints);

            MyApplication myApplication = (MyApplication)getApplicationContext();
            List<Location> savedLocations = myApplication.getMyLocations();

            lv_savedLocations.setAdapter(new ArrayAdapter<Location>( this , android.R.layout.simple_list_item_1, savedLocations ));


    }
}