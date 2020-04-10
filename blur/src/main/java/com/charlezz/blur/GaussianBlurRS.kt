package com.charlezz.blur

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur

class GaussianBlurRS(private val context: Context) : BlurEngine {
    override fun blur(image: Bitmap, radius: Int): Bitmap {
        val bitmap = image.copy(image.config, true)
        val rs = RenderScript.create(context)
        val input = Allocation.createFromBitmap(
            rs,
            image,
            Allocation.MipmapControl.MIPMAP_NONE,
            Allocation.USAGE_SCRIPT
        )
        val output = Allocation.createTyped(rs, input.type)
            ScriptIntrinsicBlur.create(rs, Element.U8_4(rs)).apply {
            setRadius(radius.toFloat())
            setInput(input)
            forEach(output)
        }
        output.copyTo(bitmap)

        return bitmap
    }

    override fun getType(): BlurType {
        return BlurType.GAUSSIAN_BLUR_RS
    }

}