package com.capstone.skincancerdetection.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Artikel(
    val photo: Int,
    val tittle: String,
    val content: String
): Parcelable
