package com.capstone.skincancerdetection.model

import android.content.res.AssetManager
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*

class Classifier(assetManager: AssetManager, modelPath: String, labelPath: String, inputSize: Int) {
    private var INTERPRETER: Interpreter
    private var LABEL_LIST: List<String>
    private val INPUT_SIZE: Int = inputSize
    private val PIXEL_SIZE: Int = 3
    private val MAX_RESULTS = 3
    private val THRESHOLD = 0.4f

    data class Recognition(
        var id: String = "",
        var title: String = "",
        var confidence: Float = 0F
    )  {
        override fun toString(): String {
            val percen = confidence*100
            return "Title = $title, Confidence = $percen%"
        }
    }

    init {
        INTERPRETER = Interpreter(loadModelFile(assetManager, modelPath))
        LABEL_LIST = loadLabelList(assetManager, labelPath)
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadLabelList(assetManager: AssetManager, labelPath: String): List<String> {
        return assetManager.open(labelPath).bufferedReader().useLines { it.toList() }

    }

    fun recognizeImage(bitmap: Bitmap): List<Classifier.Recognition> {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false)
        val byteBuffer = convertBitmapToByteBuffer(scaledBitmap)
        val result = Array(1) { FloatArray(LABEL_LIST.size) }
        INTERPRETER.run(byteBuffer, result)
        return getSortedResult(result)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * PIXEL_SIZE)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)

        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until INPUT_SIZE) {
            for (j in 0 until INPUT_SIZE) {
                val value = intValues[pixel++]

                byteBuffer.putFloat((((value.shr(16)  and 0xFF) * 2f) / 255f)-1f)
                byteBuffer.putFloat((((value.shr(8) and 0xFF)* 2f) / 255f)-1f)
                byteBuffer.putFloat((((value and 0xFF) * 2f) / 255f)-1f)
            }
        }
        return byteBuffer
    }

    private fun getSortedResult(labelProbArray: Array<FloatArray>): List<Classifier.Recognition> {
        val pq = PriorityQueue(
            MAX_RESULTS,
            Comparator<Recognition> {
                    (_, _, confidence1), (_, _, confidence2)
                -> java.lang.Float.compare(confidence1, confidence2) * -1
            })

        for (i in LABEL_LIST.indices) {
            val confidence = labelProbArray[0][i]
            if (confidence >= THRESHOLD) {
                pq.add(Classifier.Recognition("" + i,
                    if (LABEL_LIST.size > i) LABEL_LIST[i] else "Unknown", confidence)
                )
            }
        }

        val recognitions = ArrayList<Classifier.Recognition>()
        val recognitionsSize = Math.min(pq.size, MAX_RESULTS)
        for (i in 0 until recognitionsSize) {
            recognitions.add(pq.poll())
        }
        return recognitions
    }
}