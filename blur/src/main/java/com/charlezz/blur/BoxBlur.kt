package com.charlezz.blur

import android.graphics.Bitmap

class BoxBlur : BlurEngine {
    override fun blur(image: Bitmap, radius: Int): Bitmap {
        val w = image.width
        val h = image.height
        val currentPixels = IntArray(w * h)
        val newPixels = IntArray(w * h)
        image.getPixels(currentPixels, 0, w, 0, 0, w, h)
        blurProcess(w, h, currentPixels, newPixels, radius)
        return Bitmap.createBitmap(newPixels, w, h, Bitmap.Config.ARGB_8888)
    }

    private fun blurProcess(
        w: Int,
        h: Int,
        currentPixels: IntArray,
        newPixels: IntArray,
        radius: Int
    ) {
        for (col in 0 until w) {
            for (row in 0 until h) {
                newPixels[row * w + col] = getSurroundAverage(currentPixels, col, row, h, w, radius)
            }
        }
    }

    private fun getSurroundAverage(
        currentPixels: IntArray,
        col: Int,
        row: Int,
        h: Int,
        w: Int,
        radius: Int
    ): Int {
        val originalPixel = currentPixels[row * w + col]
        val alpha: Int = originalPixel ushr 24
        val originalRed = originalPixel ushr 16 and 0xFF
        val originalGreen = originalPixel ushr 8 and 0xFF
        val originalBlue = originalPixel and 0xFF

        var sumOfRed = originalRed
        var sumOfGreen = originalGreen
        var sumOfBlue = originalBlue

        for (y in (row - radius..row + radius)) {
            for (x in col - radius..col + radius) {
                if (y < 0 || y > h - 1 || x < 0 || x > w - 1) {
                    // 이미지 가장자리를 벗어나는 계산은 originalPixel 값을 더한다.
                    sumOfRed += originalRed;
                    sumOfGreen += originalGreen;
                    sumOfBlue += originalBlue
                } else if (y == row && x == col) {
                    // originalPixel은 시작할 때 이미 더 했음
                } else {
                    val sidePixel = currentPixels[y * w + x]
                    sumOfRed += sidePixel ushr 16 and 0xFF
                    sumOfGreen += sidePixel ushr 8 and 0xFF
                    sumOfBlue += sidePixel and 0xFF
                }
            }
        }

        val denominator = (radius * 2 + 1) * (radius * 2 + 1)

        return ((alpha and 0xff) shl 24) or
                ((sumOfRed / denominator) and 0xff shl 16) or
                ((sumOfGreen / denominator) and 0xff shl 8) or
                ((sumOfBlue / denominator) and 0xff)
    }

    override fun getType() = BlurType.BOX_BLUR

}