package com.franscar.instabus.ui.bus_station

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.WorkerThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.franscar.instabus.MainActivity
import com.franscar.instabus.R
import com.franscar.instabus.data.images.UserImage
import com.franscar.instabus.data.images.UserImageDao
import com.franscar.instabus.data.images.UserImageDatabase
import com.franscar.instabus.ui.home.EmptyHomeRecyclerAdapter
import com.franscar.instabus.ui.shared.SharedViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BusStationFragment : Fragment(), BusStationsRecyclerAdapter.UserImagesItemListener {

    private lateinit var userImageDao: UserImageDao
    private lateinit var recyclerView: RecyclerView
    private lateinit var navController: NavController
    private lateinit var sharedViewModel: SharedViewModel

    var userImagesData = MutableLiveData<List<UserImage>>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_bus_station, container, false)
        userImageDao = UserImageDatabase.getDatabase(requireContext()).userImageDao()

        setHasOptionsMenu(true)
        recyclerView = root.findViewById(R.id.bus_station_recycler_view)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        sharedViewModel.selectedBusStation.observe(viewLifecycleOwner, {
            (activity as MainActivity).supportActionBar?.title = it.street_name
        })

        recyclerView.adapter = EmptyHomeRecyclerAdapter(requireContext())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        return root
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch {
            getImages()
        }

        userImagesData.observe(viewLifecycleOwner, {
            recyclerView.adapter = BusStationsRecyclerAdapter(requireContext(), it, this)
        })
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

    override fun onUserImageItemClick() {
        navController.navigate(R.id.action_bus_station_to_image)
    }

    @WorkerThread
    private fun getImages() {
        val data = userImageDao.getImages(sharedViewModel.selectedBusStation.value?.street_name!!)
        userImagesData.postValue(data)
    }
}