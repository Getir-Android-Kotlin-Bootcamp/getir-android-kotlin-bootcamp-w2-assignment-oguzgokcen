package com.example.homework

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.libraries.places.api.Places

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        val mapFragment = SupportMapFragment.newInstance()
        Places.initialize(this, BuildConfig.MAPS_API_KEY)
        val MapsFragment = MapsFragment()
        supportFragmentManager
            .beginTransaction()
            .add(R.id.frameLayout,MapsFragment)
            .commit()
    }


}