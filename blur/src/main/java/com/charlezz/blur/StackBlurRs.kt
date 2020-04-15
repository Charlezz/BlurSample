package com.charlezz.blur

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import at.favre.app.blurbenchmark.ScriptC_stackblur


class StackBlurRs(private val context: Context) : BlurEngine {
    private var renderScript = RenderScript.create(context)


    override fun blur(bitmap: Bitmap, radius: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val blurScript = ScriptC_stackblur(renderScript)
        val inAllocation = Allocation.createFromBitmap(renderScript, bitmap)
        blurScript._gIn = inAllocation
        blurScript._width = width.toLong()
        blurScript._height = height.toLong()
        blurScript._radius = radius.toLong()
        var row_indices = IntArray(height)
        for (i in 0 until height) {
            row_indices[i] = i
        }
        val rows =
            Allocation.createSized(
                renderScript,
                Element.U32(renderScript),
                height,
                Allocation.USAGE_SCRIPT
            )
        rows.copyFrom(row_indices)
        row_indices = IntArray(width)
        for (i in 0 until width) {
            row_indices[i] = i
        }
        val columns =
            Allocation.createSized(
                renderScript,
                Element.U32(renderScript),
                width,
                Allocation.USAGE_SCRIPT
            )
        columns.copyFrom(row_indices)
        blurScript.forEach_blur_h(rows)
        blurScript.forEach_blur_v(columns)
        inAllocation.copyTo(bitmap)
        return bitmap
    }

    override fun getType(): BlurType = BlurType.STACK_BLUR_RS

}