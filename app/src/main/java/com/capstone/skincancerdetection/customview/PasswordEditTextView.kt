package com.example.storyapp.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.capstone.skincancerdetection.R

class PasswordEditTextView : AppCompatEditText, View.OnTouchListener {

    private lateinit var showPassIcon: Drawable
    private lateinit var passIcon: Drawable

    constructor(context: Context) : super(context){
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs){
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        init()
    }

    private fun init(){
        showPassIcon = ContextCompat.getDrawable(context, R.drawable.ic_eye_24) as Drawable
        passIcon = ContextCompat.getDrawable(context, R.drawable.ic_password_24) as Drawable

        setOnTouchListener(this)

        addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) showPassButton() else showPassIcon()
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().length < 6) showFailedPassword()
            }

        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        showPassIcon()
    }

    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        if (compoundDrawables[2] != null){
            val hidePassStart: Float
            val hidePassEnd: Float
            var isShowPassClicked = false

            if (layoutDirection == View.LAYOUT_DIRECTION_RTL){
                hidePassEnd = (showPassIcon.intrinsicWidth + paddingStart).toFloat()
                when{
                    event.x < hidePassEnd -> isShowPassClicked = true
                }
            }else{
                hidePassStart = (width - paddingEnd - showPassIcon.intrinsicWidth).toFloat()
                when{
                    event.x > hidePassStart -> isShowPassClicked = true
                }
            }

            if (isShowPassClicked){
                return when (event.action){
                    MotionEvent.ACTION_DOWN -> {
                        if(text != null) transformationMethod = HideReturnsTransformationMethod.getInstance()
                        true
                    } MotionEvent.ACTION_UP -> {
                        if(text != null) transformationMethod = PasswordTransformationMethod.getInstance()
                        true
                    }else -> false
                }
            }
        }
        return false
    }

    private fun showPassButton(){
        setButtonDrawable(startOfTheText = passIcon, endOfTheText = showPassIcon)
    }

    private fun showPassIcon(){
        setButtonDrawable(startOfTheText = passIcon)
    }

    private fun showFailedPassword(){
        error = context.getString(R.string.invalid_pass)
    }

    private fun setButtonDrawable(
        startOfTheText: Drawable? = null,
        topOfTheText: Drawable? = null,
        endOfTheText: Drawable? = null,
        bottomOfTheText: Drawable? = null,
    ){
        setCompoundDrawablesRelativeWithIntrinsicBounds(
            startOfTheText,
            topOfTheText,
            endOfTheText,
            bottomOfTheText
        )
    }
}