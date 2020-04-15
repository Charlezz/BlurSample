package com.charlezz.blur

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.util.Log
import com.hoko.blur.renderscript.ScriptC_stackblur
import kotlin.system.measureTimeMillis


class StackBlurRs(context: Context) : BlurEngine {
    private var renderScript = RenderScript.create(context)

    var inAllocation: Allocation? = null
    var outAllocation: Allocation? = null
    override fun blur(bitmap: Bitmap, radius: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val blurScript = ScriptC_stackblur(renderScript)

        inAllocation = Allocation.createFromBitmap(renderScript, bitmap)
        outAllocation = Allocation.createFromBitmap(renderScript, Bitmap.createBitmap(bitmap))

        blurScript._input = inAllocation
        blurScript._output = outAllocation
        blurScript._width = width
        blurScript._height = height
        blurScript._radius = radius

        blurScript.forEach_stackblur_v(inAllocation)

        blurScript._input = outAllocation
        blurScript._output = inAllocation

        blurScript.forEach_stackblur_h(outAllocation)

        inAllocation?.copyTo(bitmap)
        return bitmap
    }

    override fun getType(): BlurType = BlurType.STACK_BLUR_RS

}