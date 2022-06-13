package com.capstone.skincancerdetection.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.capstone.skincancerdetection.R
import com.capstone.skincancerdetection.databinding.ActivityScanBinding
import com.capstone.skincancerdetection.helper.Helper
import com.capstone.skincancerdetection.model.Classifier
import com.capstone.skincancerdetection.model.User
import org.tensorflow.lite.Interpreter
import java.io.*
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanBinding
    private lateinit var bitmapBuff: Bitmap
    private var file: File? = null

    private lateinit var user: User

    private lateinit var mClassifier: Classifier
    private lateinit var tfLite: Interpreter
    private val mInputSize = 224
    private val mModelPath = "model_skincancerdetection.tflite"
    private val mLabelPath = "label.txt"
    private val mSamplePath = "default_img.jpg"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getPermission()
        user = intent.getParcelableExtra(MainActivity.EXTRA_USER)!!

        mClassifier = Classifier(assets, mModelPath, mLabelPath, mInputSize)
        tfLite = Interpreter(loadModelFile(mModelPath))

        resources.assets.open(mSamplePath).use {
            bitmapBuff = BitmapFactory.decodeStream(it)
            bitmapBuff = Bitmap.createScaledBitmap(bitmapBuff, mInputSize, mInputSize, true)
            binding.previewImageView.setImageBitmap(bitmapBuff)
        }

        binding.apply {
            resultColumn.text = getString(R.string.select_photo)

            btnScan.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    setButtonUploadEnabled()
                }

                override fun afterTextChanged(p0: Editable?) {

                }

            })
        }

        setButtonListener()
        setButtonUploadEnabled()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSION){
            if (!allPermissionAllowed()){
                Helper.createToast(this, getString(R.string.note_have_permission))
                finish()
            }
        }
    }

    private fun allPermissionAllowed()=REQUIRED_PERMISSION.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.help_menu, menu)
        return true
    }

    private fun getPermission(){
        if (!allPermissionAllowed()){
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSION, REQUEST_CODE_PERMISSION)
        }
    }

    fun loadModelFile(path: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(path)

        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel

        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun scaleImage(bitmap: Bitmap?): Bitmap {
        val orignalWidth = bitmap!!.width
        val originalHeight = bitmap.height
        val scaleWidth = mInputSize.toFloat() / orignalWidth
        val scaleHeight = mInputSize.toFloat() / originalHeight
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(bitmap, 0, 0, orignalWidth, originalHeight, matrix, true)
    }

    private fun scanPhoto(){
        if (file != null){
            val results = mClassifier.recognizeImage(bitmapBuff).firstOrNull()
            val percen = results?.confidence?.times(100)
            binding.resultColumn.text= results?.title+"\n Percentase : $percen%"

        }else{
            Helper.createToast(this, getString(R.string.chose_photo))
        }
    }

    private val launcherCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        if (it.resultCode == CAMERA_X){
            val myFile = it.data?.getSerializableExtra("picture") as File
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean

            bitmapBuff = Helper.rotateBitmap(BitmapFactory.decodeFile(myFile.path), isBackCamera)
            val myFileTemp = bitmapToFile(bitmapBuff, myFile.path)
            file = myFileTemp

            bitmapBuff = scaleImage(bitmapBuff)
        }
        binding.apply {
            txReesult.text = resources.getString(R.string.tx_hasil)
            resultColumn.text = "Silahkan scan photo terlebih dahulu"
            previewImageView.setImageBitmap(bitmapBuff)
        }
    }

    private fun openCameraX(){
        launcherCameraX.launch(Intent(this, CameraActivity::class.java))
    }

    private val launcherGalery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == RESULT_OK){
            val imageSelected: Uri = it.data?.data as Uri
            val myFile = Helper.convertUriToFile(imageSelected, this)

            file = myFile
            val filePath = myFile.getPath()
            bitmapBuff = BitmapFactory.decodeFile(filePath)
            bitmapBuff = scaleImage(bitmapBuff)

            binding.apply {
                txReesult.text = resources.getString(R.string.tx_hasil)
                resultColumn.text = "Silahkan scan photo terlebih dahulu"
                previewImageView.setImageURI(imageSelected)
            }
        }
    }

    private fun openGalery(){
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"

        val chooserIntent = Intent.createChooser(intent, "Silahkan pilih")
        launcherGalery.launch(chooserIntent)
    }

    private fun setButtonListener(){
        binding.apply {
            btnScan.setOnClickListener {
                scanPhoto()
            }
            galleryButton.setOnClickListener { openGalery() }
            cameraButton.setOnClickListener { openCameraX() }
        }
    }

    private fun bitmapToFile(bitmap: Bitmap?, path: String): File{
        val file = File(path)
        var out: OutputStream? = null
        try {
            file.createNewFile()
            out = FileOutputStream(file)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }finally {
            out?.close()
        }
        return file
    }

    private fun setButtonUploadEnabled(){
        binding.btnScan.isEnabled = binding.previewImageView.drawable != null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.help -> {
                startActivity(Intent(this, ScanHelpActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {

        const val EXTRA_USER = "user"

        const val CAMERA_X = 200
        private const val REQUEST_CODE_PERMISSION = 10
        private val REQUIRED_PERMISSION = arrayOf(Manifest.permission.CAMERA)
    }
}