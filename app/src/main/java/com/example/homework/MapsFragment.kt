package com.example.homework

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.ktx.widget.PlaceSelectionError
import com.google.android.libraries.places.ktx.widget.PlaceSelectionSuccess
import com.google.android.libraries.places.ktx.widget.placeSelectionEvents
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import kotlinx.coroutines.launch

class MapsFragment : Fragment(),OnMapReadyCallback{
    private var map: GoogleMap? = null
    private var cameraPosition: CameraPosition? = null
    private lateinit var currentLocation: Location
    lateinit var fusedLocationProviderClient:FusedLocationProviderClient
    private var locationPermissionGranted = false
    lateinit var frameLayout:FrameLayout
    lateinit var cardView:View
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment
        frameLayout= requireView().findViewById(R.id.frameCard)
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,Place.Field.ADDRESS))
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireContext())
        val googleMap = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        googleMap?.getMapAsync(this)

        lifecycleScope.launch {
            autocompleteFragment.placeSelectionEvents().collect { event ->
                when (event) {
                    is PlaceSelectionSuccess -> {
                        val place = event.place
                        val latLng = place.latLng
                        val lat = latLng?.latitude
                        val lng = latLng?.longitude
                        val location = LatLng(lat!!, lng!!)
                        googleMap?.getMapAsync(callPlaces(location,place.address!!))
                    }
                    is PlaceSelectionError -> Toast.makeText(
                        this@MapsFragment.context,
                        "Failed to get place '${event.status.statusMessage}'",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        val registerForActivityLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                getCurrentLocation()
            } else {
                Toast.makeText(this.context,"Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
        if(locationPermissionGranted){
            getCurrentLocation()
        }else{
            getLocationPermission(registerForActivityLauncher)
        }
    }

    private fun callPlaces(coordinate:LatLng,name:String):OnMapReadyCallback{
        return OnMapReadyCallback { googleMap ->
            map?.addMarker(MarkerOptions().position(coordinate).title("Current Location"))
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate,DEFAULT_ZOOM.toFloat()))
            cardView.findViewById<TextView>(R.id.locationText).text = name
        }
    }
    override fun onMapReady(map: GoogleMap) {
        this.map = map
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this.requireContext(),R.raw.style))

    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(){
        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                locationPermissionGranted = true
                currentLocation= task.result
                val location = LatLng(currentLocation.latitude, currentLocation.longitude)
                map?.addMarker(MarkerOptions().position(location).title("Current Location"))
                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(location,DEFAULT_ZOOM.toFloat()))
                //bring a dialog box that informs user
                cardView = layoutInflater.inflate(R.layout.info_card, frameLayout, true)
                cardView.findViewById<TextView>(R.id.locationText).text = "Latitude: ${currentLocation.latitude} \nLongitude: ${currentLocation.longitude}"

            }else{
                Toast.makeText(
                    this@MapsFragment.context,
                    "Failed to get current location",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getLocationPermission(registerForActivityLauncher: ActivityResultLauncher<String>){
        if (ActivityCompat.checkSelfPermission(this.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
            getCurrentLocation()
        }else {
            registerForActivityLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 20
    }

}