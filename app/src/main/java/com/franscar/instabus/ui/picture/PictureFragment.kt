package com.franscar.instabus.ui.picture

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.franscar.instabus.R
import com.franscar.instabus.data.images.UserImage
import com.franscar.instabus.data.images.UserImageDatabase
import com.franscar.instabus.ui.camera.CameraViewModel
import com.franscar.instabus.ui.shared.SharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.DateFormat.getDateInstance
import java.text.DateFormat.getTimeInstance
import java.util.*

class PictureFragment : Fragment() {

    private lateinit var navController: NavController
    private lateinit var cameraViewModel: CameraViewModel
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_picture, container, false)
        val image = root.findViewById<ImageView>(R.id.picture)
        setHasOptionsMenu(true)

        if (requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_NO) {
            root.findViewById<RelativeLayout>(R.id.picture_fragment).setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        }

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        cameraViewModel = ViewModelProvider(requireActivity()).get(CameraViewModel::class.java)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        var rotation = 0
        when (ExifInterface(cameraViewModel.imageSrc.value!!).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_270 -> rotation = 270
            ExifInterface.ORIENTATION_ROTATE_180 -> rotation = 180
            ExifInterface.ORIENTATION_ROTATE_90 -> rotation = 90
        }

        // Doesn't need fancy bitmap rotation function, but loads the image really slow (about a second) ?
        // Glide.with(context).load(File(cameraViewModel.imageSrc.value!!)).crossFade().into(image)

        image.setImageBitmap(rotate(BitmapFactory.decodeFile((cameraViewModel.imageSrc.value!!).absolutePath), rotation))

        return root
    }

    private fun rotate(bitmap: Bitmap, degrees: Int): Bitmap {
        var image = bitmap
        if (degrees != 0) {
            val matrix = Matrix()
            matrix.setRotate(
                    degrees.toFloat(),
                    image.width.toFloat() / 2,
                    image.height.toFloat() / 2
            )
            try {
                val newImage = Bitmap.createBitmap(
                        image, 0, 0, image.width,
                        image.height, matrix, true
                )
                if (image != newImage) {
                    image.recycle()
                    image = newImage
                }
            } catch (ex: OutOfMemoryError) {
                throw ex
            }
        }
        return image
    }

    override fun onViewCreated(root: View, savedInstanceState: Bundle?) {
        super.onViewCreated(root, savedInstanceState)

        val userImageDao = UserImageDatabase.getDatabase(requireContext()).userImageDao()
        val name = root.findViewById<EditText>(R.id.picture_name)

        root.findViewById<Button>(R.id.save_picture_button).setOnClickListener {

            if (name.text.toString().isEmpty()) {
                Toast.makeText(context, "Please enter a name for your picture.", Toast.LENGTH_SHORT).show()
            } else {
                sharedViewModel.canGetImages = false

                runBlocking {
                    val job: Job = launch(context = Dispatchers.IO) {
                        userImageDao.insertImage(
                            UserImage(
                                0,
                                cameraViewModel.imageSrc.value.toString(),
                                name.text.toString(),
                                getDateInstance().format(Date()) + " - " + getTimeInstance().format(Date()),
                                ViewModelProvider(requireActivity()).get(SharedViewModel::class.java).selectedBusStation.value?.street_name!!
                            )
                        )
                    }
                    job.join()
                    sharedViewModel.canGetImages = true
                    job.join()
                    navController.navigateUp()
                    job.join()
                    navController.navigateUp()
                }

                Toast.makeText(context, "Picture saved successfully.", Toast.LENGTH_SHORT).show()

                view?.let {
                    val keyboard = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    keyboard.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            navController.navigateUp()
        }
        return super.onOptionsItemSelected(item)
    }

}