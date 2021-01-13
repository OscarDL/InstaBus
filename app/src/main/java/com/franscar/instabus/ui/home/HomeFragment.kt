package com.franscar.instabus.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.franscar.instabus.R
import com.franscar.instabus.data.BusStations
import com.franscar.instabus.data.BusStationsService
import com.franscar.instabus.utilities.FileHelper
import com.franscar.instabus.utilities.WEB_SERVICE_URL
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


class HomeFragment : Fragment(), HomeRecyclerAdapter.BusStationsItemListener {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var navController: NavController

    private lateinit var swipeLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)

        recyclerView = root.findViewById(R.id.home_list_recycler_view)
        swipeLayout = root.findViewById(R.id.home_list_swipe_layout)
        swipeLayout.setOnRefreshListener {
            val busStationsData = MutableLiveData<List<BusStations>>()

            CoroutineScope(Dispatchers.IO).launch {
                val moshi = Moshi.Builder()
                        .add(KotlinJsonAdapterFactory())
                        .build()
                val retrofit = Retrofit.Builder()
                        .baseUrl(WEB_SERVICE_URL)
                        .addConverterFactory(MoshiConverterFactory.create(moshi))
                        .build()
                val service = retrofit.create(BusStationsService::class.java)
                val serviceData = service.getBusStationsData().body()?.data?.nearstations
                Log.i("ResponseData", serviceData.toString())
                busStationsData.postValue(serviceData ?: emptyList())
                Log.i("BusStationsRepository", "PULLING_DATA_FROM_WEB")

                if (serviceData != null) {
                    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                    val listType = Types.newParameterizedType(List::class.java, BusStations::class.java)
                    val adapter: JsonAdapter<List<BusStations>> = moshi.adapter(listType)

                    val json = adapter.toJson(serviceData)
                    FileHelper.saveToTextFile(requireActivity().application, json)
                    Log.i("BusStationsRepository", "SAVED_DATA_TO_CACHE")
                }
                swipeLayout.isRefreshing = false

//              val currentFragment = requireParentFragment().childFragmentManager.fragments
//              // loop through list in case there are multiple fragments, even if it should only have one anyway
//              for (fragment in currentFragment) {
//                  if (fragment.toString().substringBefore('{') == "HomeFragment") {
//                      val fragmentTransaction: FragmentTransaction = parentFragmentManager.beginTransaction()
//                      fragmentTransaction.detach(fragment)
//                      fragmentTransaction.attach(fragment)
//                      fragmentTransaction.commit()
//                  }
//              }
            }
        }

        // Show empty recyclerView until data is loaded
        recyclerView.adapter = EmptyHomeRecyclerAdapter(requireContext())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        homeViewModel.busStationsData.observe(viewLifecycleOwner, Observer {
            recyclerView.adapter = HomeRecyclerAdapter(requireContext(), it, this)
        })

        return root
    }

    override fun onBusStationItemClick(busStation: BusStations) {
        navController.navigate(R.id.action_home_to_bus_station)
    }
}