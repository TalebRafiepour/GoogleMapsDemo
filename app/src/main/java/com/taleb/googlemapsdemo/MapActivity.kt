package com.taleb.googlemapsdemo

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import kotlinx.android.synthetic.main.activity_map.*
import java.io.IOException
import java.lang.Exception
import java.util.*

class MapActivity : AppCompatActivity() {

    private var googleMap: GoogleMap? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        initUI()
    }

    private fun initUI() {
        initSearchEditText()
        initMap()
        initCurrentLocationButton()
    }

    private fun initCurrentLocationButton() {
        if(getMapLocationPermissions()){
            locateCurrentLocationButton.setOnClickListener {
                getDeviceCurrentLocation()
            }
        }
    }

    private fun initSearchEditText() {
        if (getMapLocationPermissions()) {
            searchOnMapEditText.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || event.action == KeyEvent.ACTION_DOWN
                    || event.action == KeyEvent.KEYCODE_ENTER
                ) {
                    geoLocate()
                }
                return@setOnEditorActionListener false
            }
        } else {
            //todo something when location permission not granted
        }
    }


    private fun getMapLocationPermissions(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, MAP_PERMISSIONS_REQUEST)
                return false
            }
        }

        return true
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MAP_PERMISSIONS_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initMap()
            }
        }
    }

    private fun initMap() {
        if (getMapLocationPermissions()) {
            val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
            mapFragment.getMapAsync(OnMapReadyCallback { googleMap ->
                this.googleMap = googleMap
                if (getMapLocationPermissions()) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED
                    )
                        return@OnMapReadyCallback
                    this.googleMap?.isMyLocationEnabled = true
                    this.googleMap?.uiSettings?.isMyLocationButtonEnabled = false
                    getDeviceCurrentLocation()
                }
            })
        }
    }


    private fun getDeviceCurrentLocation() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this) as FusedLocationProviderClient

        try {
            if (getMapLocationPermissions()) {
                val lastLocation = fusedLocationProviderClient!!.lastLocation
                lastLocation.addOnCompleteListener { task ->
                    val currentLocation = task.result
                    if (currentLocation != null) {
                        zoomMapCamera(
                            LatLng(currentLocation.latitude, currentLocation.longitude), DEFAULT_MAP_ZOOM, getString(
                                R.string.my_location
                            )
                        )
                    } else {
                        //gps is off
                    }
                }
            } else {
                Toast.makeText(this, "unable to get current location.", Toast.LENGTH_LONG).show()
            }

        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }


    private fun zoomMapCamera(latLng: LatLng, zoom: Float, title: String) {
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))

        if (title != getString(R.string.my_location)) {
            val markerOptions = MarkerOptions()
                .position(latLng)
                .title(title)
            googleMap?.addMarker(markerOptions)
        }
    }


    private fun geoLocate() {
        val searchString = searchOnMapEditText.text.toString()
        val geoCoder = Geocoder(this, Locale.getDefault())
        try {
            val addressList = geoCoder.getFromLocationName(searchString, 1)
            if (addressList.isNotEmpty()) {
                val address = addressList[0]
                zoomMapCamera(LatLng(address.latitude, address.longitude), DEFAULT_MAP_ZOOM, address.getAddressLine(0))
            } else {
                //todo something when address not found
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("", "${e.message}")
        }

    }


    companion object {
        private const val MAP_PERMISSIONS_REQUEST = 23
        private const val DEFAULT_MAP_ZOOM = 15f
    }
}
