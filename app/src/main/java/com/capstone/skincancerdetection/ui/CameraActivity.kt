package com.capstone.skincancerdetection.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.capstone.skincancerdetection.R
import com.capstone.skincancerdetection.databinding.ActivityCameraBinding
import com.capstone.skincancerdetection.helper.Helper
import com.capstone.skincancerdetection.ui.ScanActivity.Companion.CAMERA_X
import java.lang.Exception

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private var imageCapture: ImageCapture? = null
    private var camerSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setButtonListener()
    }

    override fun onResume() {
        super.onResume()
        startCamera()
    }

    private fun startCamera(){
        val cameraProvider = ProcessCameraProvider.getInstance(this)
        cameraProvider.addListener({
            val cameraProv: ProcessCameraProvider = cameraProvider.get()
            val preview = Preview.Builder()
                .build().also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }
            imageCapture = ImageCapture.Builder().build()
            try {
                cameraProv.unbindAll()
                cameraProv.bindToLifecycle(this, camerSelector, preview, imageCapture)
            }catch (e: Exception){
                Helper.createToast(this, getString(R.string.failed_open_cam))
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto(){
        val imgCapture = imageCapture?: return
        val filePhoto = Helper.createFile(application)

        val outputOption = ImageCapture.OutputFileOptions.Builder(filePhoto).build()
        imgCapture.takePicture(outputOption, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback{
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val intent = Intent()
                intent.putExtra("picture", filePhoto)
                intent.putExtra("isBackCamera", camerSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                setResult(CAMERA_X, intent)
                finish()
            }

            override fun onError(exception: ImageCaptureException) {
                Helper.createToast(this@CameraActivity, getString(R.string.failed_take_photo))
            }
        })
    }

    private fun setButtonListener(){
        binding.apply {
            btnCapture.setOnClickListener { takePhoto() }
            btnSwitchCamera.setOnClickListener {
                camerSelector = if (camerSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                startCamera()
            }
        }
    }
}