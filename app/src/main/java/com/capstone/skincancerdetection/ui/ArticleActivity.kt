package com.capstone.skincancerdetection.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.capstone.skincancerdetection.R
import com.capstone.skincancerdetection.databinding.ActivityArtikelBinding
import com.capstone.skincancerdetection.model.Artikel

class ArticleActivity : AppCompatActivity() {

    private lateinit var artikelItem: Artikel
    private lateinit var binding: ActivityArtikelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtikelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar = supportActionBar

        if (actionBar != null){
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_24)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        artikelItem = intent.getParcelableExtra(EXTRA_ARTIKEL)!!

        binding.apply {
            imgArtikel.setImageResource(artikelItem.photo)
            txTittle.text = artikelItem.tittle
            txIsiArtikel.text = artikelItem.content
        }

    }

    companion object{
        const val EXTRA_ARTIKEL = "artikel"
    }
}