package com.capstone.skincancerdetection.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    var email: String? = null,
    var name: String? = null,
    var isLogin: Boolean = false
): Parcelable



