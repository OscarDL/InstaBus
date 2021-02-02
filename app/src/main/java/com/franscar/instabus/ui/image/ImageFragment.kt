package com.franscar.instabus.ui.image

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.franscar.instabus.MainActivity
import com.franscar.instabus.R
import com.franscar.instabus.data.images.UserImage
import com.franscar.instabus.data.images.UserImageDao
import com.franscar.instabus.data.images.UserImageDatabase
import com.franscar.instabus.ui.shared.SharedViewModel
import kotlinx.coroutines.*
import java.io.File
import java.text.DateFormat
import java.util.*

class ImageFragment : Fragment() {

    private lateinit var prefs: SharedPreferences
    private lateinit var navController: NavController
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_image, container, false)
        setHasOptionsMenu(true)

        if (requireContext().resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_NO) {
            root.findViewById<RelativeLayout>(R.id.image_fragment).setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        sharedViewModel.selectedImage.observe(viewLifecycleOwner, {
            (activity as MainActivity).supportActionBar!!.title = it.title
        })

        val image = root.findViewById<ImageView>(R.id.image)

        if (File(sharedViewModel.selectedImage.value!!.image).exists()) {

            var rotation = 0
            when (ExifInterface(sharedViewModel.selectedImage.value!!.image).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_270 -> rotation = 270
                ExifInterface.ORIENTATION_ROTATE_180 -> rotation = 180
                ExifInterface.ORIENTATION_ROTATE_90 -> rotation = 90
            }

            image.setImageBitmap(rotate(BitmapFactory.decodeFile(sharedViewModel.selectedImage.value!!.image), rotation))

            // Doesn't need fancy bitmap rotation function, but loads the image really slow (about a second) ?
            // Glide.with(context).load(File(sharedViewModel.selectedImage.value!!.image)).crossFade().into(image)

            root.findViewById<TextView>(R.id.image_date).text =
                    String.format(resources.getString(R.string.taken_on), sharedViewModel.selectedImage.value?.date)
        } else {
            root.findViewById<TextView>(R.id.image_date).text = String.format(resources.getString(R.string.no_picture))
        }

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

        root.findViewById<Button>(R.id.delete_image_button).setOnClickListener {

            if (prefs.getBoolean("delete_confirmation_prompt", true)) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Confirmation")
                    .setMessage("Do you really want to delete this picture?")
                    .setPositiveButton(" DELETE ") { _, _ ->
                        sharedViewModel.canGetImages = false
                        deleteImage(userImageDao)
                    }
                    .setNegativeButton(" CANCEL ") { _, _ -> }
                    .create()
                    .show()
            } else {
                sharedViewModel.canGetImages = false
                deleteImage(userImageDao)
            }

        }
    }

    private fun deleteImage(userImageDao: UserImageDao) {
        runBlocking {
            val job: Job = launch(context = Dispatchers.IO) {
                userImageDao.deleteImage(sharedViewModel.selectedImage.value!!.date)
                if (File(sharedViewModel.selectedImage.value!!.image).exists())
                    File(sharedViewModel.selectedImage.value!!.image).delete()
            }
            job.join()
            sharedViewModel.canGetImages = true
            job.join()
            navController.navigateUp()
        }
        Toast.makeText(context, "Picture successfully deleted.", Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            navController.navigateUp()
        }
        return super.onOptionsItemSelected(item)
    }

}
