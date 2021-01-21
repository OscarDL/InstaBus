package com.franscar.instabus.ui.bus_station

import android.R.attr.endColor
import android.R.attr.startColor
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
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
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import java.io.File


class BusStationFragment : Fragment(), BusStationsRecyclerAdapter.UserImagesItemListener {

    private lateinit var userImageDao: UserImageDao
    private lateinit var recyclerView: RecyclerView
    private lateinit var navController: NavController
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var swipeBackgroundColor: ColorDrawable
    private lateinit var busStationAdapter: BusStationsRecyclerAdapter

    var userImagesData = MutableLiveData<List<UserImage>>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_bus_station, container, false)
        userImageDao = UserImageDatabase.getDatabase(requireContext()).userImageDao()

        setHasOptionsMenu(true)
        recyclerView = root.findViewById(R.id.bus_station_recycler_view)
        swipeBackgroundColor = ColorDrawable(Color.parseColor("#FF709DE0"))
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        sharedViewModel.selectedBusStation.observe(viewLifecycleOwner, {
            (activity as MainActivity).supportActionBar?.title = it.street_name
        })

        recyclerView.adapter = EmptyHomeRecyclerAdapter(requireContext())
            //BusStationsRecyclerAdapter(requireContext(), mutableListOf(), this)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        return root
    }

    override fun onViewCreated(root: View, savedInstanceState: Bundle?) {
        super.onViewCreated(root, savedInstanceState)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)
        root.findViewById<FloatingActionButton>(R.id.add_photo).setOnClickListener {
            navController.navigate(R.id.action_bus_station_to_camera)
        }
    }

    override fun onResume() {
        super.onResume()
        /** Issue: image card sometimes randomly doesn't appear after taking picture
            (Gets images before new image was inserted into the local room database)

            This runBlocking tries to get new image after it was inserted, but can't
            confirm it works because I'm not able to reproduce the issue anymore...?
        */
        runBlocking {
            val job: Job = launch(context = Dispatchers.IO) {
                while (!sharedViewModel.canGetImages) {
                    Log.i("WAITING", "Getting images...")
                }
                getImages()
            }
            job.join()
            userImagesData.observe(viewLifecycleOwner, {
                recyclerView.adapter = BusStationsRecyclerAdapter(requireContext(), it as MutableList<UserImage>, this@BusStationFragment)
                busStationAdapter = BusStationsRecyclerAdapter(requireContext(), it, this@BusStationFragment)
            })
        }
    }

    // SWIPE CARD TO LEFT
    private val itemTouchHelperCallback: ItemTouchHelper.SimpleCallback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
        ): Boolean { return false }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, position: Int) {
            val realPosition = viewHolder.adapterPosition
            val swipedItem = userImagesData.value!![realPosition]

            busStationAdapter.removeItem(realPosition)

            val snack = Snackbar.make(viewHolder.itemView, "Picture successfully deleted.", Snackbar.LENGTH_LONG).setAction("RESTORE") {
                busStationAdapter.restoreItem(realPosition, swipedItem)
                swipeJob(true, swipedItem)
            }
            snack.show()

            snack.addCallback(object : Snackbar.Callback() {
                override fun onDismissed(snackbar: Snackbar?, event: Int) {
                    if (event == DISMISS_EVENT_TIMEOUT) {
                        if (File(swipedItem.image).exists())
                            File(swipedItem.image).delete()
                    }
                }
            })

            swipeJob(false, swipedItem)
        }

        override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
        ) {
            val deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete_24px)!!
            val iconMargin = (viewHolder.itemView.height - deleteIcon.intrinsicHeight) / 2
            if (dX > 0) {
                swipeBackgroundColor.setBounds(
                    viewHolder.itemView.left + 0,
                    viewHolder.itemView.top + 0,
                    dX.toInt() + 0,
                    viewHolder.itemView.bottom + 0
                )
                deleteIcon.setBounds(
                    viewHolder.itemView.left + iconMargin,
                    viewHolder.itemView.top + iconMargin,
                    viewHolder.itemView.left + iconMargin + deleteIcon.intrinsicWidth,
                    viewHolder.itemView.bottom - iconMargin
                )
            } else if (dX < 0) {
                swipeBackgroundColor.setBounds(
                    viewHolder.itemView.left + dX.toInt(),
                    viewHolder.itemView.top + 0,
                    viewHolder.itemView.right + 0,
                    viewHolder.itemView.bottom + 0
                )
                deleteIcon.setBounds(
                    viewHolder.itemView.right - iconMargin - deleteIcon.intrinsicWidth,
                    viewHolder.itemView.top + iconMargin,
                    viewHolder.itemView.right - iconMargin,
                    viewHolder.itemView.bottom - iconMargin
                )
            } else {
                swipeBackgroundColor.setBounds(0, 0, 0, 0)
            }
            swipeBackgroundColor.draw(c)

            c.save()

            if (dX > 0) {
                c.clipRect(
                    viewHolder.itemView.left + 0,
                    viewHolder.itemView.top + 0,
                    dX.toInt() + 0,
                    viewHolder.itemView.bottom + 0
                )
            } else {
                c.clipRect(
                    viewHolder.itemView.left + dX.toInt(),
                    viewHolder.itemView.top + 0,
                    viewHolder.itemView.right + 0,
                    viewHolder.itemView.bottom + 0
                )
            }

            deleteIcon.draw(c)

            c.restore()

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            navController.navigateUp()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onUserImageItemClick(userImage: UserImage) {
        sharedViewModel.selectedImage.value = userImage
        navController.navigate(R.id.action_bus_station_to_image)
    }

    private fun swipeJob(insert: Boolean, image: UserImage) {
        runBlocking {
            val job: Job = launch(context = Dispatchers.IO) {
                if (insert) {
                    userImageDao.insertImage(image)
                } else {
                    userImageDao.deleteImage(image.date)
                }
            }
            job.join() // Use job.join() to refresh UI only when data is actually updated
            userImagesData.observe(viewLifecycleOwner, {
                recyclerView.adapter = BusStationsRecyclerAdapter(requireContext(),
                        it as MutableList<UserImage>, this@BusStationFragment)
            })
        }
        /*CoroutineScope(Dispatchers.IO).launch {
            if (insert) {
                userImageDao.insertImage(image)
            } else {
                userImageDao.deleteImage(image.date)
            }

            // Use Dispatcher to refresh UI only when data is actually updated
            withContext(Dispatchers.Main) {
                userImagesData.observe(viewLifecycleOwner, {
                    recyclerView.adapter = BusStationsRecyclerAdapter(requireContext(), it as MutableList<UserImage>, this@BusStationFragment)
                })
            }
        }*/
    }

    @WorkerThread
    private fun getImages() {
        val data = userImageDao.getImages(sharedViewModel.selectedBusStation.value?.street_name!!)
        userImagesData.postValue(data)
    }
}