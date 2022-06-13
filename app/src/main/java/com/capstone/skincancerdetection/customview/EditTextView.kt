package com.capstone.skincancerdetection.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.capstone.skincancerdetection.R
import com.capstone.skincancerdetection.helper.Helper

class EditTextView : AppCompatEditText, View.OnTouchListener {

    private lateinit var clearButtonImage: Drawable
    private var iconName: Drawable? = null

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
        clearButtonImage = ContextCompat.getDrawable(context, R.drawable.ic_close_24) as Drawable
        iconName = ContextCompat.getDrawable(context, R.drawable.ic_person_24) as Drawable

        setOnTouchListener(this)

        addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty()) showClearButton() else showIcon()
            }

            override fun afterTextChanged(s: Editable?) {
                if (!isNameValid(s.toString())) showInvalidName()
            }

        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        textAlignment = View.TEXT_ALIGNMENT_VIEW_START
        showIcon()
    }

    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        if (compoundDrawables[2] != null){
            val clearButtonStart: Float
            val clearButtonEnd: Float
            var isClearButtonClicked = false

            if (layoutDirection == View.LAYOUT_DIRECTION_RTL){
                clearButtonEnd = (clearButtonImage.intrinsicWidth + paddingStart).toFloat()
                when{
                    event.x < clearButtonEnd -> isClearButtonClicked = true
                }
            }else{
                clearButtonStart = (width - paddingEnd - clearButtonImage.intrinsicWidth).toFloat()
                when{
                    event.x > clearButtonStart -> isClearButtonClicked = true
                }
            }

            if (isClearButtonClicked){
                return when (event.action){
                    MotionEvent.ACTION_DOWN -> {
                        clearButtonImage = ContextCompat.getDrawable(context, R.drawable.ic_close_24) as Drawable
                        showClearButton()
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        clearButtonImage = ContextCompat.getDrawable(context, R.drawable.ic_close_24) as Drawable
                        when{
                            text != null -> text?.clear()
                        }
                        showIcon()
                        true
                    }else -> false
                }
            }else{
                return false
            }
        }
        return false
    }

    private fun showClearButton(){
        setButtonDrawable(startOfTheText = iconName, endOfTheText = clearButtonImage)
    }

    private fun showIcon(){
        setButtonDrawable(startOfTheText = iconName)
    }

    private fun isNameValid(name: CharSequence): Boolean {
        return Helper.isNameFormatValid(name)
    }

    private fun showInvalidName(){
        error = context.getString(R.string.invalid_name)
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