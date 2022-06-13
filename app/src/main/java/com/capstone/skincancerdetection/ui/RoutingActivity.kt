package com.capstone.skincancerdetection.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.capstone.skincancerdetection.databinding.ActivityRoutingBinding
import com.capstone.skincancerdetection.model.User

class RoutingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoutingBinding
    private lateinit var userModel: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoutingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userModel = intent.getParcelableExtra(EXTRA_RESULT)!!
        if (userModel.name == "") {
            binding.tvGreating.text = "Hai ${userModel.name}"
        }else{
            binding.tvGreating.text = "Hai You"
        }


        buttonListener()
    }

    private fun buttonListener(){
        binding.btnContinue.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(MainActivity.EXTRA_USER, userModel)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra(MainActivity.EXTRA_USER, userModel)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }
    companion object{
        const val EXTRA_RESULT = "extra result"
    }
}