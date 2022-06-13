package com.capstone.skincancerdetection.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import com.capstone.skincancerdetection.R
import com.capstone.skincancerdetection.databinding.ActivityScanHelpBinding

class ScanHelpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)

        val width = dm.widthPixels
        val height = dm.heightPixels

        window.setLayout((width *.8).toInt(), (height *.6).toInt())
    }
}