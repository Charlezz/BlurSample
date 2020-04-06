package com.charlezz.blur

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun optimizedBlurBox() {
        val engine = BoxBlurOptimized()
        val imageArray = intArrayOf(
            1, 2, 3,
            4, 5, 6,
            7, 8, 9
        )
        val blurredArray = IntArray(9)
        engine.blurProcess(3, 3, imageArray, blurredArray, 1)


        val expect = intArrayOf(
            2, 3, 3,
            4, 5, 5,
            6, 7, 7
        )

        for (i in imageArray.indices) {
            assertEquals(expect[i], blurredArray[i])
        }
    }

}
