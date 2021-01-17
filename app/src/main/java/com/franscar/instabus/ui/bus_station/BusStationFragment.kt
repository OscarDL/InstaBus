package com.franscar.instabus.ui.bus_station

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.franscar.instabus.MainActivity
import com.franscar.instabus.R
import com.franscar.instabus.ui.shared.SharedViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class BusStationFragment : Fragment() {

    private lateinit var navController: NavController
    private lateinit var busStationViewModel: SharedViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        setHasOptionsMenu(true)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        busStationViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        busStationViewModel.selectedBusStation.observe(viewLifecycleOwner, {
            (activity as MainActivity).supportActionBar?.title = it.street_name
        })

        return inflater.inflate(R.layout.fragment_bus_station, container, false)
    }

    override fun onViewCreated(root: View, savedInstanceState: Bundle?) {
        super.onViewCreated(root, savedInstanceState)
        root.findViewById<FloatingActionButton>(R.id.add_photo).setOnClickListener {
            navController.navigate(R.id.action_bus_station_to_camera)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            navController.navigateUp()
        }
        return super.onOptionsItemSelected(item)
    }
}