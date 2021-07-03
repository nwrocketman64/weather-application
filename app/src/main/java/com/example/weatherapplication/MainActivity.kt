package com.example.weatherapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.getInstance

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Create the variables for the GPS location.
        lateinit var locationManager: LocationManager
        var hasGPS: Boolean
        var locationGPS : Location? = null

        // The default url for getting the weather information. Set to Sandpoint, Idaho
        var currentUrl = "https://api.openweathermap.org/data/2.5/weather?lat=48.2766&lon=-116.5535&appid=f35b728b9784b1eaf04baa7a3e381718&units=imperial"

        // Get the objects from layout view
        val imageIcon: ImageView = findViewById(R.id.weatherIcon)
        val town: TextView = findViewById(R.id.town)
        val currCond: TextView = findViewById(R.id.currCond)
        val currTemp: TextView = findViewById(R.id.currTemp)
        val maxTemp: TextView = findViewById(R.id.maxTemp)
        val minTemp: TextView = findViewById(R.id.minTemp)
        val currHum: TextView = findViewById(R.id.currHum)
        val windSpeed: TextView = findViewById(R.id.windSpeed)
        val lastUpdated: TextView = findViewById(R.id.lastUpdated)
        val locationData: TextView = findViewById(R.id.locationData)
        val button: Button = findViewById(R.id.refreshButton)

        // Create a queue for Google Volley.
        val queue = Volley.newRequestQueue(this)

        // The function makes a request to openweathermap.org to get the weather data and puts it on screen.
        fun makeRequest() {
            // Update the Url with the current location data.
            currentUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" + locationGPS!!.latitude.toString() + "&lon=" + locationGPS!!.longitude.toString() + "&appid=f35b728b9784b1eaf04baa7a3e381718&units=imperial"

            // Create the JsonObjectRequest.
            val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, currentUrl, null,
                { response ->
                    // If the request worked, create the needed values from the data.
                    val weather = response.get("weather")
                    val main = response.get("main")
                    val wind = response.get("wind")

                    // Update the town name.
                    town.text = response["name"].toString()

                    // Update the current condition text box.
                    var fronttrim = weather.toString().substringAfter("description\":\"")
                    currCond.text = fronttrim.substring(0, fronttrim.indexOf("\","))

                    // Get the icon name from the data.
                    fronttrim = weather.toString().substringAfter("icon\":\"")

                    // Create the icon Url and load the image using Glide.
                    Glide.with(this).load("http://openweathermap.org/img/wn/" + fronttrim.substring(0, fronttrim.indexOf("\"")) + "@2x.png").into(imageIcon)

                    // Get the current temperature.
                    fronttrim = main.toString().substringAfter("temp\":")
                    currTemp.text = "Current Temp: " + fronttrim.substring(0, fronttrim.indexOf(",")) + "F"
                    fronttrim = main.toString().substringAfter("temp_max\":")
                    maxTemp.text = "High: " + fronttrim.substring(0, fronttrim.indexOf(",")) + "F"
                    fronttrim = main.toString().substringAfter("temp_min\":")
                    minTemp.text = "Low: " + fronttrim.substring(0, fronttrim.indexOf(",")) + "F"

                    // Get the humidity.
                    fronttrim = main.toString().substringAfter("humidity\":")
                    currHum.text = "Humidity: " + fronttrim.substring(0, fronttrim.indexOf("}")) + "%"

                    // Get the wind speed.
                    fronttrim = wind.toString().substringAfter("speed\":")
                    windSpeed.text = "Wind Speed: " + fronttrim.substring(0, fronttrim.indexOf(",")) + "mph"

                    // Update the current time.
                    // Create the time object and formate
                    val time: Calendar = getInstance()
                    val sdf: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

                    // Update the time to screen.
                    lastUpdated.text = "Last Updated: " + sdf.format(time.time)
                },
                { error ->
                    // If there was an error, Log the error.
                    Log.e(error.toString(), "Error")
                })

            // Add the object to the queue and make the request.
            queue.add(jsonObjectRequest)
        }

        // The function get the GPS location.
        fun getLocation() {
            // Create the location manager.
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            hasGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            // Check that the device supports GPS.
            if (hasGPS){
                // If so, check that the device has permission.
                Log.d("CodeAndroidLocation", "hasGPS")
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // If not, ask the user for permission.
                    val permList = arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                    )
                    ActivityCompat.requestPermissions(this@MainActivity, permList, 1)
                    return
                }
                // Request an update for the location.
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,
                    0F,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            if (location != null) {
                                locationGPS = location
                            }
                        }

                        override fun onStatusChanged(
                            provider: String?,
                            status: Int,
                            extras: Bundle?
                        ) {
                            super.onStatusChanged(provider, status, extras)
                        }

                        override fun onProviderEnabled(provider: String) {
                            super.onProviderEnabled(provider)
                        }

                        override fun onProviderDisabled(provider: String) {
                            super.onProviderDisabled(provider)
                        }
                    })

                val localGPSLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (localGPSLocation != null) {
                    // Get the location and set it to locationGPS
                    locationGPS = localGPSLocation
                    // Update locationData on the screen.
                    locationData.text = "Latitude: " + locationGPS!!.latitude.toString() + " Longitude: " + locationGPS!!.longitude.toString()
                }
            } else {
                // If there is no location, get the location running.
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        }

        // Init the app.
        getLocation()
        makeRequest()

        // Create a button listener.
        button.setOnClickListener {
            // Update the data.
            getLocation()
            makeRequest()
        }
    }
}