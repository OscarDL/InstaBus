package com.franscar.instabus.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.franscar.instabus.R
import com.franscar.instabus.data.BusStation
import com.franscar.instabus.data.BusStationsService
import com.franscar.instabus.ui.shared.SharedViewModel
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

    private lateinit var homeViewModel: SharedViewModel
    private lateinit var navController: NavController

    private lateinit var swipeLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        homeViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)

        recyclerView = root.findViewById(R.id.home_list_recycler_view)
        swipeLayout = root.findViewById(R.id.home_list_swipe_layout)
        swipeLayout.setOnRefreshListener {
            val busStationsData = MutableLiveData<List<BusStation>>()

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
                    val listType = Types.newParameterizedType(List::class.java, BusStation::class.java)
                    val adapter: JsonAdapter<List<BusStation>> = moshi.adapter(listType)

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

        // SWIPE CARD TO LEFT
        /*val itemTouchHelperCallback: ItemTouchHelper.SimpleCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    // Row is swiped from recycler view
                    // remove it from adapter
                }
            }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)*/

        homeViewModel.busStationsData.observe(viewLifecycleOwner, {
            recyclerView.adapter = HomeRecyclerAdapter(requireContext(), it, this)
        })

        return root
    }

    override fun onBusStationItemClick(busStation: BusStation) {
        homeViewModel.selectedBusStation.value = busStation
        navController.navigate(R.id.action_home_to_bus_station)
    }
}