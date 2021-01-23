package com.franscar.instabus.ui.map

import android.Manifest
import android.app.AlertDialog
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.preference.PreferenceManager
import com.franscar.instabus.BuildConfig
import com.franscar.instabus.R
import com.franscar.instabus.ui.shared.SharedViewModel
import com.franscar.instabus.utilities.refreshData
import com.franscar.instabus.utilities.refreshMap
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay

class MapFragment : Fragment() {

    private lateinit var prefs: SharedPreferences
    private lateinit var navController: NavController
    private lateinit var mapViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_map, container, false)

        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        mapViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)

        // if (prefs.getBoolean("enable_location", true)) checkLocationPermission()

        val ctx = requireActivity().applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        val map = root.findViewById<MapView>(R.id.map)

        if (prefs.getBoolean("enable_multi_touch_rotation", true)) {
            val rotationGestures = RotationGestureOverlay(map)
            rotationGestures.isEnabled = true
            map.overlays.add(rotationGestures)
        }

        map.minZoomLevel = 3.5
        map.maxZoomLevel = 21.0
        map.setUseDataConnection(true)
        map.setMultiTouchControls(true)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)

        val gPoint = GeoPoint(41.3985182, 2.1917991)

        if(prefs.getBoolean("enable_map_animation", true) && refreshMap) {
            map.controller.setZoom(5.0)
            map.controller.setCenter(GeoPoint(40.9, 1.7))
            Handler(Looper.getMainLooper()).postDelayed({
                map.controller.animateTo(gPoint,
                prefs.getInt("default_map_zoom", 31)/2.toDouble(),
                2000)
            }, 200)
            refreshMap = false
        } else {
            map.controller.setZoom(prefs.getInt("default_map_zoom", 31)/2.toDouble())
            map.controller.setCenter(gPoint)
        }

        val compassOverlay = CompassOverlay(requireContext(), map)
        compassOverlay.enableCompass()
        map.overlays.add(compassOverlay)

        val defaultPin = getDrawable(requireContext(), R.drawable.ic_current_location_24dp)
        var stationPin = getDrawable(requireContext(), R.drawable.ic_location_24dp)
        stationPin = stationPin?.mutate()
        stationPin = DrawableCompat.wrap(stationPin!!)
        @Suppress("DEPRECATION")
        DrawableCompat.setTint(stationPin, resources.getColor(R.color.red))

        val defaultMarker = Marker(map)
        defaultMarker.icon = defaultPin
        defaultMarker.position = gPoint
        defaultMarker.title = "Your current location"
        defaultMarker.snippet = "(41.3985182, 2.1917881)"
        defaultMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        map.overlays.add(defaultMarker)

        mapViewModel.busStationsData.observe(viewLifecycleOwner, {
            for (item in it) {
                val busMarker = Marker(map)
                busMarker.icon = stationPin
                busMarker.position = GeoPoint(item.lat, item.lon)
                busMarker.setOnMarkerClickListener { _, _ ->
                    AlertDialog.Builder(requireContext())
                        .setTitle(item.street_name)
                        .setMessage(if (item.buses.sumBy { if (item.buses.contains('-')) 1 else 0 } > 0) {
                            "Buses: "
                        } else {
                            "Bus: "
                        } + item.buses
                                + "\n" + ((item.distance * 1000).toInt()).toString().replaceFirst(
                            "^0+",
                            ""
                        ) + "m away")
                        .setPositiveButton(" View Photos ") { _, _ ->
                            mapViewModel.selectedBusStation.value = item
                            navController.navigate(R.id.action_map_to_bus_station)
                        }
                        .create()
                        .show()
                    true
                }
                map.overlays.add(busMarker)
            }
        })

        return root
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            AlertDialog.Builder(requireContext())
                .setTitle("Location Access Request")
                .setMessage("Enabling in-app location will help you know where and how far the bus stations are relative to you.\nYou can change this option later in settings.")
                .setPositiveButton("ACCEPT") { _, _ ->
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                }
                .setNegativeButton("DENY") { _, _ ->
                    prefs.edit().putBoolean("enable_location", false).apply()
                }
                .create()
                .show()

        }
    }

}