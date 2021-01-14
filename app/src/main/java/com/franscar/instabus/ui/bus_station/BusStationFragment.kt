package com.franscar.instabus.ui.bus_station

import android.os.Bundle
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
import com.franscar.instabus.databinding.FragmentBusStationBinding
import com.franscar.instabus.ui.shared.SharedViewModel

class BusStationFragment : Fragment() {

    private lateinit var navController: NavController
    private lateinit var busStationViewModel: SharedViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        busStationViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        (activity as MainActivity).supportActionBar?.title = busStationViewModel.selectedBusStation.value?.street_name

        val binding = FragmentBusStationBinding.inflate(inflater, container, false)
        binding.busStationViewModel = busStationViewModel
        binding.lifecycleOwner = this
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            navController.navigateUp()
        }
        return super.onOptionsItemSelected(item)
    }
}