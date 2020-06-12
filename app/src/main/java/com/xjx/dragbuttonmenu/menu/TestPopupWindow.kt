package com.xjx.dragbuttonmenu.menu

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.xjx.dragbuttonmenu.R

class TestPopupWindow : PopupWindow{

    constructor(context: Context?) :super(context){
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        isOutsideTouchable = true
        isFocusable = true
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val contentView: View = LayoutInflater.from(context).inflate(R.layout.popup_test_vertical, null, false)
        setContentView(contentView)
    }
}