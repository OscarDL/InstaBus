package com.franscar.instabus.ui.image

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.franscar.instabus.MainActivity
import com.franscar.instabus.R
import com.franscar.instabus.data.images.UserImage
import com.franscar.instabus.data.images.UserImageDatabase
import com.franscar.instabus.ui.shared.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.DateFormat
import java.util.*

class ImageFragment : Fragment() {

    private lateinit var navController: NavController
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_image, container, false)

        setHasOptionsMenu(true)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        sharedViewModel.selectedImage.observe(viewLifecycleOwner, {
            (activity as MainActivity).supportActionBar?.title = it.title
        })

        val image = root.findViewById<ImageView>(R.id.image)

        if (File(sharedViewModel.selectedImage.value?.image!!).exists()) {
            image.setImageBitmap(BitmapFactory.decodeFile((sharedViewModel.selectedImage.value?.image)))

            //TODO: FIX ROTATION ISSUE, UNDER SHOULD NOT BE NECESSARY AND CREATES LAYOUT BOUND ISSUES

            when (ExifInterface(sharedViewModel.selectedImage.value?.image!!).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_270 -> image.rotation = 270f
                ExifInterface.ORIENTATION_ROTATE_180 -> image.rotation = 180f
                ExifInterface.ORIENTATION_ROTATE_90 -> image.rotation = 90f
            }

            root.findViewById<TextView>(R.id.image_date).text =
                    String.format(resources.getString(R.string.taken_on), sharedViewModel.selectedImage.value?.date)
        } else {
            root.findViewById<TextView>(R.id.image_date).text = String.format(resources.getString(R.string.no_picture))
        }

        return root
    }

    override fun onViewCreated(root: View, savedInstanceState: Bundle?) {
        super.onViewCreated(root, savedInstanceState)
        val userImageDao = UserImageDatabase.getDatabase(requireContext()).userImageDao()

        root.findViewById<Button>(R.id.delete_image_button).setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmation")
                .setMessage("Do you really want to delete this picture?")
                .setPositiveButton(" DELETE ") { _, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        userImageDao.deleteImage(sharedViewModel.selectedImage.value!!.date)
                        if(File(sharedViewModel.selectedImage.value!!.image).exists())
                            File(sharedViewModel.selectedImage.value!!.image).delete()
                    }
                    Toast.makeText(context, "Picture successfully deleted.", Toast.LENGTH_SHORT).show()
                    navController.navigateUp()
                }
                .setNegativeButton(" CANCEL ") { _, _ -> }
                .create()
                .show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            navController.navigateUp()
        }
        return super.onOptionsItemSelected(item)
    }

}