package com.charlezz.blur

import android.graphics.Bitmap
import java.util.*

class BoxBlurOptimized: AbstractBlur {

    override fun blur(image: Bitmap, radius: Int): Bitmap {
        val w = image.width
        val h = image.height
        val currentPixels = IntArray(w * h)
        val newPixels = IntArray(w * h)
        image.getPixels(currentPixels, 0, w, 0, 0, w, h)
        blurProcess(w, h, currentPixels, newPixels, radius)
        return Bitmap.createBitmap(newPixels, w, h, Bitmap.Config.ARGB_8888)
    }

    fun blurProcess(
        w: Int,
        h: Int,
        currentPixels: IntArray,
        newPixels: IntArray,
        radius: Int
    ) {
        val firstPassPixel = IntArray(w * h)
        val denominator = (radius * 2 + 1)

        for (row in 0 until h) {
            val rQueue = LinkedList<Int>()
            val gQueue = LinkedList<Int>()
            val bQueue = LinkedList<Int>()

            val startIndex = row * w
            val originalPixel = currentPixels[startIndex]
            val rOrig = originalPixel ushr 16 and 0xFF
            val gOrig = originalPixel ushr 8 and 0xFF
            val bOrig = originalPixel and 0xFF

            repeat(radius + 1) {
                rQueue.add(rOrig)
                gQueue.add(gOrig)
                bQueue.add(bOrig)
            }

            var rSum = rOrig * (radius + 1)
            var gSum = gOrig * (radius + 1)
            var bSum = bOrig * (radius + 1)

            for (col in 1..radius) {
                // In the event of width is smaller than radius
                val nextPixelIndex = startIndex + if (col > w - 1) w - 1 else col
                val nextPixel = currentPixels[nextPixelIndex]
                val rNext = nextPixel ushr 16 and 0xFF
                val gNext = nextPixel ushr 8 and 0xFF
                val bNext = nextPixel and 0xFF

                rQueue.add(rNext)
                gQueue.add(gNext)
                bQueue.add(bNext)

                rSum += rNext
                gSum += gNext
                bSum += bNext
            }

            for (col in 0 until w) {
                val newPixelIndex = row * w + col
                firstPassPixel[newPixelIndex] =
                    (currentPixels[newPixelIndex] and -0x1000000) /* which is 0xff000000 to get the original alpha */ or
                            ((rSum / denominator) and 0xff shl 16) or
                            ((gSum / denominator) and 0xff shl 8) or
                            ((bSum / denominator) and 0xff)

                rSum -= rQueue.remove()
                gSum -= gQueue.remove()
                bSum -= bQueue.remove()

                val nextPixelIndex =
                    if (col + 1 + radius > w - 1)
                        (row + 1) * w - 1
                    else row * w + col + radius + 1

                val nextPixel = currentPixels[nextPixelIndex]
                val rNext = nextPixel ushr 16 and 0xFF
                val gNext = nextPixel ushr 8 and 0xFF
                val bNext = nextPixel and 0xFF

                rQueue.add(rNext)
                gQueue.add(gNext)
                bQueue.add(bNext)

                rSum += rNext
                gSum += gNext
                bSum += bNext
            }
        }

        for (col in 0 until w) {
            val rQueue = LinkedList<Int>()
            val gQueue = LinkedList<Int>()
            val bQueue = LinkedList<Int>()

            val originalPixel = firstPassPixel[col]
            val rOrig = originalPixel ushr 16 and 0xFF
            val gOrig = originalPixel ushr 8 and 0xFF
            val bOrig = originalPixel and 0xFF

            repeat(radius + 1) {
                rQueue.add(rOrig)
                gQueue.add(gOrig)
                bQueue.add(bOrig)
            }

            var rSum = rOrig * (radius + 1)
            var gSum = gOrig * (radius + 1)
            var bSum = bOrig * (radius + 1)

            for (row in 1..radius) {
                // In the event of width is smaller than radius
                val nextPixelIndex = col + if (row > h - 1) (h - 1) * w else row * w
                val nextPixel = firstPassPixel[nextPixelIndex]
                val rNext = nextPixel ushr 16 and 0xFF
                val gNext = nextPixel ushr 8 and 0xFF
                val bNext = nextPixel and 0xFF

                rQueue.add(rNext)
                gQueue.add(gNext)
                bQueue.add(bNext)

                rSum += rNext
                gSum += gNext
                bSum += bNext
            }

            for (row in 0 until h) {
                val newPixelIndex = row * w + col
                newPixels[newPixelIndex] =
                    (firstPassPixel[newPixelIndex] and -0x1000000) /* which is 0xff000000 to get the original alpha */ or
                            ((rSum / denominator) and 0xff shl 16) or
                            ((gSum / denominator) and 0xff shl 8) or
                            ((bSum / denominator) and 0xff)

                rSum -= rQueue.remove()
                gSum -= gQueue.remove()
                bSum -= bQueue.remove()

                val nextPixelIndex =
                    if (row + 1 + radius > h - 1)
                        ((row + 1) * w) + col
                    else (row + radius + 1) * w + col

                if (nextPixelIndex >= w * h) break

                val nextPixel = firstPassPixel[nextPixelIndex]
                val rNext = nextPixel ushr 16 and 0xFF
                val gNext = nextPixel ushr 8 and 0xFF
                val bNext = nextPixel and 0xFF

                rQueue.add(rNext)
                gQueue.add(gNext)
                bQueue.add(bNext)

                rSum += rNext
                gSum += gNext
                bSum += bNext
            }
        }
    }

    override fun getType() = BlurType.BOX_BLUR_OPTIMIZED


}
