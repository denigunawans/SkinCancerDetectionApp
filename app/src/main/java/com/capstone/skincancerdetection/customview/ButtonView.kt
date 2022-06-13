package com.capstone.skincancerdetection.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.capstone.skincancerdetection.R

class ButtonView : AppCompatButton {

    private lateinit var activeBackground: Drawable
    private lateinit var offBackground: Drawable
    private var txtColor: Int = 0

    private var textButton: String = ""

    constructor(context: Context) : super(context){
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs){
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        init()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        background = if (isEnabled) activeBackground else offBackground

        setTextColor(txtColor)
        textSize = 12f
        gravity = Gravity.CENTER
        text = textButton
    }

    private fun init(){
        txtColor = ContextCompat.getColor(context, android.R.color.background_light)
        activeBackground = ContextCompat.getDrawable(context, R.drawable.bg_button_active) as Drawable
        offBackground = ContextCompat.getDrawable(context, R.drawable.bg_button_off) as Drawable
    }

    fun setTextButton(text: String){
        textButton = text
    }

}