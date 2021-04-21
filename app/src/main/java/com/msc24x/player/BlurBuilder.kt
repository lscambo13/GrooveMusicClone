package com.msc24x.player

import android.content.Context
import android.graphics.Bitmap
import androidx.renderscript.Allocation
import androidx.renderscript.Element
import androidx.renderscript.RenderScript
import androidx.renderscript.ScriptIntrinsicBlur
import kotlin.math.roundToInt

class BlurBuilder {
    private val bitmapScale = 0.1f
    private val blurRadius = 25.0f

    fun blur(context: Context?, image: Bitmap): Bitmap {
        val width = (image.width * bitmapScale).roundToInt()
        val height = (image.height * bitmapScale).roundToInt()
        val inputBitmap = Bitmap.createScaledBitmap(image, width, height, false)
        val outputBitmap = Bitmap.createBitmap(inputBitmap)
        val rs: RenderScript = RenderScript.create(context)
        val theIntrinsic: ScriptIntrinsicBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        val tmpIn: Allocation = Allocation.createFromBitmap(rs, inputBitmap)
        val tmpOut: Allocation = Allocation.createFromBitmap(rs, outputBitmap)
        theIntrinsic.setRadius(blurRadius)
        theIntrinsic.setInput(tmpIn)
        theIntrinsic.forEach(tmpOut)
        tmpOut.copyTo(outputBitmap)
        return outputBitmap
    }
}