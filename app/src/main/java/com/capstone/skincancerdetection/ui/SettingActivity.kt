package com.capstone.skincancerdetection.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.capstone.skincancerdetection.R
import com.capstone.skincancerdetection.databinding.ActivitySettingBinding
import com.capstone.skincancerdetection.model.User

class SettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomnavview.setOnItemSelectedListener {
            when (it.itemId){
                R.id.home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra(MainActivity.EXTRA_USER, user)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
                R.id.history -> {
                    val intent = Intent(this, HistoryActivity::class.java)
                    intent.putExtra(HistoryActivity.EXTRA_USER, user)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
                R.id.setting -> {
                    val intent = Intent(this, SettingActivity::class.java)
                    intent.putExtra(SettingActivity.EXTRA_USER, user)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
                else -> {
                    val intent = Intent(this, ProfilActivity::class.java)
                    intent.putExtra(ProfilActivity.EXTRA_USER, user)
                    startActivity(intent)
                }
            }
            true
        }

        scanListener()
    }

    private fun goIntent(cls: Class<*>){
        val intent = Intent(this, cls)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun scanListener(){
        binding.btnScan.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
        }
    }

    companion object{
        const val EXTRA_USER = "user"
    }
}