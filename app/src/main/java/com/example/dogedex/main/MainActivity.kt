package com.example.dogedex.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.dogedex.LABEL_PATH
import com.example.dogedex.MODEL_PATH
import com.example.dogedex.api.ApiResponseStatus
import com.example.dogedex.api.ApiServiceInterceptor
import com.example.dogedex.auth.LoginActivity
import com.example.dogedex.databinding.ActivityMainBinding
import com.example.dogedex.dogdetail.DogDetailActivity
import com.example.dogedex.dogdetail.DogDetailActivity.Companion.DOG_KEY
import com.example.dogedex.dogdetail.DogDetailActivity.Companion.IS_RECOGNITION_KEY
import com.example.dogedex.doglist.DogListActivity
import com.example.dogedex.machinelearning.Classifier
import com.example.dogedex.machinelearning.DogRecognition
import com.example.dogedex.model.Dog
import com.example.dogedex.model.User
import com.example.dogedex.settings.SettingsActivity
import org.tensorflow.lite.support.common.FileUtil
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var classifier: Classifier
    private var isCameraReady = false
    private val viewmodel: MainViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // open camera
                setupCamera()
            } else {
                Toast.makeText(
                    this,
                    "You need to accept camera permission to use camera",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = User.getLoggedInUser(this)
        if (user == null) {
            openLoginActivity()
            return
        } else {
            ApiServiceInterceptor.setSettionToken(user.authenticationToken)
        }

        binding.settingsFab.setOnClickListener {
            openSettingsActivity()
        }

        binding.dogListFab.setOnClickListener {
            openDogListActivity()
        }

        viewmodel.status.observe(this) {
            when (it) {
                is ApiResponseStatus.Error -> {
                    binding.loadingWheel.visibility = View.GONE
                    Toast.makeText(this, it.messageId, Toast.LENGTH_SHORT).show()
                }
                is ApiResponseStatus.Loading ->  binding.loadingWheel.visibility = View.VISIBLE
                is ApiResponseStatus.Succcess ->  binding.loadingWheel.visibility = View.GONE
            }

        }

        viewmodel.dog.observe(this) {
            if (it != null) {
                openDogDetailActivity(it)
            }
        }

        viewmodel.dogRecognition.observe(this) {
            enabledTakePhotoButton(it)
        }

        requestCameraPermission()
    }

    private fun openDogDetailActivity(dog: Dog) {
        val intent =  Intent(this, DogDetailActivity::class.java)
        intent.putExtra(DOG_KEY,dog)
        intent.putExtra(IS_RECOGNITION_KEY, true)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        viewmodel.setupClassifier(
            FileUtil.loadMappedFile(this@MainActivity, MODEL_PATH),
            FileUtil.loadLabels(this@MainActivity, LABEL_PATH)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
    }

    private fun setupCamera() {
        binding.cameraPreview.post {
            imageCapture = ImageCapture.Builder()
                .setTargetRotation(binding.cameraPreview.display.rotation)
                .build()
            cameraExecutor = Executors.newSingleThreadExecutor()
            startCamera()
            isCameraReady = true
        }
    }

    private fun requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // open camera
                    setupCamera()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
                -> {
                    AlertDialog.Builder(this)
                        .setTitle("Aceptame por gavor")
                        .setMessage("Acepta la camara o me da amsiedad")
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                        .setNegativeButton(android.R.string.cancel) { _, _ ->


                        }.show()
                }
                else -> {
                    requestPermissionLauncher.launch(
                        Manifest.permission.CAMERA
                    )
                }

            }
        } else {
            // open camera
            setupCamera()
        }
    }

/*    private fun takePhoto() {
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(getOutputPhotoFile()).build()
        imageCapture.takePicture(outputFileOptions, cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(error: ImageCaptureException) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error taking photo ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // insert your code here.

                }

            })
    }*/

/*    private fun getOutputPhotoFile(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name) + ".jpg").apply { mkdir() }
        }
        return if (mediaDir != null && mediaDir.exists()) {
            mediaDir
        } else {
            filesDir
        }
    }*/

    private fun startCamera() {
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(binding.cameraPreview.surfaceProvider)

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                viewmodel.recognizeImage(imageProxy)
            }


            cameraProvider.bindToLifecycle(
                this, cameraSelector,
                preview, imageCapture, imageAnalysis
            )
        }, ContextCompat.getMainExecutor(this))
    }

    private fun enabledTakePhotoButton(dogRecognition: DogRecognition) {
        if (dogRecognition.confidence > 70.0) {
            binding.takePhotoFab.alpha = 1F
            binding.takePhotoFab.setOnClickListener {
                viewmodel.getDogByMlId(dogRecognition.id)
            }
        } else {
            binding.takePhotoFab.alpha = 0.2F
            binding.takePhotoFab.setOnClickListener(null)
        }
    }

    private fun openDogListActivity() {
        startActivity(Intent(this, DogListActivity::class.java))

    }

    private fun openSettingsActivity() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun openLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}