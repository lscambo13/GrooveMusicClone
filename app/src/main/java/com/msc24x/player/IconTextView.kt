package com.msc24x.player

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import androidx.annotation.RequiresApi


class IconTextView : androidx.appcompat.widget.AppCompatTextView {

    @RequiresApi(Build.VERSION_CODES.O)
    constructor(context: Context) : super(context) {
        createView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        createView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createView() {
        val fontFamily = Typeface.create(resources.getFont(R.font.segmdl2), Typeface.NORMAL)
        setTypeface(fontFamily, Typeface.NORMAL)
    }
}