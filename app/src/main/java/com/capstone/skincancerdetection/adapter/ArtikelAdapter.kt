package com.capstone.skincancerdetection.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.capstone.skincancerdetection.R
import com.capstone.skincancerdetection.databinding.ItemArtikelBinding
import com.capstone.skincancerdetection.model.Artikel
import com.capstone.skincancerdetection.ui.ArticleActivity

class ArtikelAdapter : RecyclerView.Adapter<ArtikelAdapter.ListViewHolder>() {
    private val listArtikel = ArrayList<Artikel>()

    inner class ListViewHolder(val binding: ItemArtikelBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(detailArtikel: Artikel){
            binding.apply {
                imgArtikelItem.setImageResource(R.drawable.benignskincancer)
                txTittleArtikel.text = detailArtikel.tittle
                txContentArtikel.text = detailArtikel.content

                //artikel click
                root.setOnClickListener {
                    val intent = Intent(it.context, ArticleActivity::class.java)
                    intent.putExtra(ArticleActivity.EXTRA_ARTIKEL, detailArtikel)
                    it.context.startActivity(intent)
                }
            }
        }
    }

    fun setList(artikelList: List<Artikel>){
        listArtikel.clear()
        listArtikel.addAll(artikelList)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ArtikelAdapter.ListViewHolder {
        val view = ItemArtikelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtikelAdapter.ListViewHolder, position: Int) {
        holder.bind(listArtikel[position])
    }

    override fun getItemCount(): Int = listArtikel.size
}