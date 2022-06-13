package com.capstone.skincancerdetection.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.skincancerdetection.R
import com.capstone.skincancerdetection.adapter.ArtikelAdapter
import com.capstone.skincancerdetection.databinding.ActivityMainBinding
import com.capstone.skincancerdetection.model.Artikel
import com.capstone.skincancerdetection.model.User

class MainActivity : AppCompatActivity(){

    private lateinit var binding: ActivityMainBinding
    private lateinit var user: User
    private lateinit var artikelAdapter: ArtikelAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        user = intent.getParcelableExtra(EXTRA_USER)!!

        artikelAdapter = ArtikelAdapter()

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
        setListArtikel()
        setRecyclerViewArtikel()
    }

    private fun setListArtikel(){
        val artikelList = ArrayList<Artikel>()
        val tittleArtikel = resources.getStringArray(R.array.tittle_artikel)
        val contentArtikel = resources.getStringArray(R.array.content_artikel)

        var i = 0
        tittleArtikel.forEach { tittle ->
            val artikel = Artikel(
                photo = R.drawable.ic_person_24,
                tittle = tittle,
                content = contentArtikel[i]
            )
            artikelList.add(artikel)
            i++
        }
        artikelAdapter.setList(artikelList)
    }

    private fun setRecyclerViewArtikel(){
        binding.apply {
            rvArtikel.layoutManager = LinearLayoutManager(this@MainActivity)
            rvArtikel.setHasFixedSize(true)
            rvArtikel.adapter = artikelAdapter
        }
    }

    private fun scanListener(){
        binding.btnScan.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            intent.putExtra(ScanActivity.EXTRA_USER, user)
            startActivity(intent)
        }
    }

    companion object{
        const val EXTRA_USER = "user"
    }

}