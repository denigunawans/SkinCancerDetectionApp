package com.capstone.skincancerdetection.helper

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Patterns
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.capstone.skincancerdetection.R
import java.io.*
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

object Helper {
    private const val FORMAT_FILE_NAME = "dd-MM-yyyy"

    private val timeStamp: String = SimpleDateFormat(FORMAT_FILE_NAME, Locale.US).format(System.currentTimeMillis())

    fun isEmailAddressValid(emailAddress: CharSequence): Boolean{
        return Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()
    }

    fun isNameFormatValid(name: CharSequence): Boolean{
        return name.all { it.isLetter() }
    }

    fun createToast(context: Context, message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setFormatDate(dateString: String, targetTimeZone: String): String{
        val instant = Instant.parse(dateString)
        val dateFormater = DateTimeFormatter.ofPattern("dd MMM yyyy | HH:mm")
            .withZone(ZoneId.of(targetTimeZone))

        return dateFormater.format(instant)
    }

    fun createFile(application: Application): File {
        val fileDirectory = application.externalMediaDirs.firstOrNull()?.let {
            File(it, application.resources.getString(R.string.app_name)).apply {
                mkdirs()
            }
        }
        val resultDirectory = if (fileDirectory != null && fileDirectory.exists()) fileDirectory else application.filesDir
        return File(resultDirectory, "$timeStamp.jpg")
    }

    fun convertUriToFile(imgUri: Uri, context: Context): File {
        val contentResolver: ContentResolver = context.contentResolver
        val file = createTemporaryFile(context)

        val inputStream = contentResolver.openInputStream(imgUri) as InputStream
        val outputStream: OutputStream = FileOutputStream(file)
        val byteArr = ByteArray(1024)
        var len: Int

        while (inputStream.read(byteArr).also {
                len = it
            } > 0) {
            outputStream.write(byteArr, 0, len)
        }
        outputStream.close()
        inputStream.close()

        return file
    }

    fun reduceFile(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)
        var compressFile = 85
        var streamLength: Int
        do {
            val bitmapStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressFile, bitmapStream)
            val bitmapToByteArr = bitmapStream.toByteArray()
            streamLength = bitmapToByteArr.size
            compressFile -= 5
        }while (streamLength > 1000000)
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressFile, FileOutputStream(file))

        return file
    }

    fun rotateBitmap(bitmap: Bitmap, isBackCamera: Boolean = false): Bitmap {
        val matrix = Matrix()

        return if (isBackCamera){
            matrix.postRotate(90f)
            Bitmap.createBitmap(bitmap,0,0, bitmap.width, bitmap.height, matrix, true)
        }else{
            matrix.postRotate(-90f)
            matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height/2f)
            Bitmap.createBitmap(bitmap, 0,0, bitmap.width, bitmap.height, matrix, true)
        }
    }

     fun createTemporaryFile(context: Context): File {
        val directoryFile: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(timeStamp, ".jpg", directoryFile)
    }
}