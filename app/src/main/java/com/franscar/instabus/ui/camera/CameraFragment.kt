package com.franscar.instabus.ui.camera

// USING GOOGLE CAMERA X API

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.franscar.instabus.R
import com.franscar.instabus.ui.picture.PictureFragment
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private lateinit var outputDirectory: File
    private lateinit var navController: NavController
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraViewModel: CameraViewModel
    private lateinit var orientationEventListener: OrientationEventListener

    private var imageCapture: ImageCapture? = null
    private var selectedCamera = CameraSelector.DEFAULT_BACK_CAMERA

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        cameraViewModel = ViewModelProvider(requireActivity()).get(CameraViewModel::class.java)
        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(root: View, savedInstanceState: Bundle?) {
        super.onViewCreated(root, savedInstanceState)

        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        if (allPermissionsGranted()) {
            startCamera(selectedCamera)
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        outputDirectory = getOutputDirectory()

        camera_button.setOnClickListener { takePhoto() }
        flip_button.setOnClickListener { flipCamera(selectedCamera) }

        orientationEventListener = object: OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                // Monitors orientation values to determine the target rotation value
                val rotation: Int
                if (orientation in 45..134) {
                    rotation = Surface.ROTATION_270
                    root.findViewById<ImageButton>(R.id.flash_button).rotation = 270f
                    root.findViewById<ImageButton>(R.id.flip_button).rotation = 270f
                } else if (orientation in 135..224) {
                    rotation = Surface.ROTATION_180
                    root.findViewById<ImageButton>(R.id.flash_button).rotation = 180f
                    root.findViewById<ImageButton>(R.id.flip_button).rotation = 180f
                } else if (orientation in 225..314) {
                    rotation = Surface.ROTATION_90
                    root.findViewById<ImageButton>(R.id.flash_button).rotation = 90f
                    root.findViewById<ImageButton>(R.id.flip_button).rotation = 90f
                } else {
                    rotation = Surface.ROTATION_0
                    root.findViewById<ImageButton>(R.id.flash_button).rotation = 0f
                    root.findViewById<ImageButton>(R.id.flip_button).rotation = 0f
                }

                imageCapture?.targetRotation = rotation
            }
        }

        orientationEventListener.enable()

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun flipCamera(camera: CameraSelector) {
        selectedCamera =
            if (camera == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
        cameraExecutor.shutdown()
        startCamera(selectedCamera)
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Add timestamp to output file name
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    cameraExecutor.shutdown()
                    cameraViewModel.imageSrc.value = photoFile
                    navController.navigate(R.id.action_camera_to_picture)
                }
            })
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        @Suppress("Deprecation")
        val mediaDir = requireContext().externalMediaDirs.firstOrNull().let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir.exists())
            mediaDir else requireContext().filesDir
    }

    private fun startCamera(cameraSelector: CameraSelector) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(camera_viewfinder.surfaceProvider)
                }

            val camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            flash_button.setOnClickListener {
                if (camera.cameraInfo.hasFlashUnit()) {
                    if (camera.cameraInfo.torchState.value == TorchState.OFF) {
                        camera.cameraControl.enableTorch(true)
                        flash_button.setImageResource(R.drawable.ic_flash_on_36dp)
                    } else {
                        camera.cameraControl.enableTorch(false)
                        flash_button.setImageResource(R.drawable.ic_flash_off_36dp)
                    }
                } else {
                    Toast.makeText(requireContext(), "No flash unit found for this camera.", Toast.LENGTH_SHORT).show()
                }
            }

            imageCapture = ImageCapture.Builder().build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera(selectedCamera)
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
                return
            }
        }
    }

    override fun onPause() {
        super.onPause()
        cameraExecutor.shutdown()
    }

    override fun onResume() {
        super.onResume()
        startCamera(selectedCamera)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        orientationEventListener.disable()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_USER
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val REQUEST_CODE_PERMISSIONS = 2
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            navController.navigateUp()
        }
        return super.onOptionsItemSelected(item)
    }


// USING CAMERA VIEW 3RD PARTY API

/*import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.franscar.instabus.R
import com.google.android.material.snackbar.Snackbar
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.FileCallback
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Audio
import com.otaliastudios.cameraview.controls.Mode
import com.otaliastudios.cameraview.size.AspectRatio
import com.otaliastudios.cameraview.size.SizeSelectors.aspectRatio
import java.io.File

class CameraFragment : Fragment() {

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        navController = Navigation.findNavController(requireActivity(), R.id.bus_station_fragment)
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(root: View, savedInstanceState: Bundle?) {
        super.onViewCreated(root, savedInstanceState)

        //val app = Application()
        val camera: CameraView = root.findViewById(R.id.camera_viewfinder)
        camera.setLifecycleOwner(viewLifecycleOwner)

        camera.addCameraListener(object: CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                /*val file = File(app.cacheDir, "picture.png")
                val fileCallback = FileCallback {
                    Snackbar.make(root, "Failed to save picture", Snackbar.LENGTH_SHORT).show()
                }
                result.toFile(file, fileCallback);*/
                Snackbar.make(root, "Picture Taken!", Snackbar.LENGTH_SHORT).show()
            }
        })
        camera.mode = Mode.PICTURE
        camera.audio = Audio.OFF

        root.findViewById<Button>(R.id.camera_button).setOnClickListener {
            camera.takePicture()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            navController.navigateUp()
        }
        return super.onOptionsItemSelected(item)
    }
}*/}