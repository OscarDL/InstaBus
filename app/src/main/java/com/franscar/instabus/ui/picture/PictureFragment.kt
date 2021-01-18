package com.franscar.instabus.ui.picture

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.franscar.instabus.R
import com.franscar.instabus.data.images.UserImage
import com.franscar.instabus.data.images.UserImageDatabase
import com.franscar.instabus.ui.camera.CameraViewModel
import com.franscar.instabus.ui.shared.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DateFormat.getDateTimeInstance
import java.util.*

class PictureFragment : Fragment() {

    private lateinit var navController: NavController
    private lateinit var cameraViewModel: CameraViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_picture, container, false)
        val image = root.findViewById<ImageView>(R.id.picture)

        setHasOptionsMenu(true)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        cameraViewModel = ViewModelProvider(requireActivity()).get(CameraViewModel::class.java)

        //TODO: FIX ROTATION ISSUE, UNDER SHOULD NOT BE NECESSARY AND CREATES LAYOUT BOUND ISSUES

        when (ExifInterface(cameraViewModel.imageSrc.value!!).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_270 -> image.rotation = 270f
            ExifInterface.ORIENTATION_ROTATE_180 -> image.rotation = 180f
            ExifInterface.ORIENTATION_ROTATE_90 -> image.rotation = 90f
        }

        image.setImageBitmap(BitmapFactory.decodeFile((cameraViewModel.imageSrc.value)?.absolutePath))

        return root
    }

    override fun onViewCreated(root: View, savedInstanceState: Bundle?) {
        super.onViewCreated(root, savedInstanceState)

        val userImageDao = UserImageDatabase.getDatabase(requireContext()).userImageDao()
        val name = root.findViewById<EditText>(R.id.picture_name)

        root.findViewById<Button>(R.id.save_picture_button).setOnClickListener {

            CoroutineScope(Dispatchers.IO).launch {
                userImageDao.insertImage(
                    UserImage(
                        0,
                        cameraViewModel.imageSrc.value.toString(),
                        name.text.toString(),
                        getDateTimeInstance().format(Date()),
                        ViewModelProvider(requireActivity()).get(SharedViewModel::class.java).selectedBusStation.value?.street_name!!
                    )
                )
            }
            Toast.makeText(context, "Picture saved successfully.", Toast.LENGTH_SHORT).show()

            //navController.navigate(R.id.action_picture_to_bus_station)
            navController.navigateUp()
            navController.navigateUp()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            navController.navigateUp()
        }
        return super.onOptionsItemSelected(item)
    }

}