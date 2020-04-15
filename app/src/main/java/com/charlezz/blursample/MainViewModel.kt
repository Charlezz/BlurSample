package com.charlezz.blursample

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.renderscript.RenderScript
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.charlezz.blur.*


class MainViewModel(application: Application) : AndroidViewModel(application) {
    val maxRadius = 25
    val latestMeasureTimeMills = MutableLiveData<Long>().apply { value = 0 }
    val blurType = MutableLiveData<BlurType>().apply { value = BlurType.BOX_BLUR }
    val liveRadius = MutableLiveData<Int>().apply { value = 5 }
    val bmpImage = MutableLiveData<Bitmap>().apply {
        value = BitmapFactory.decodeResource(application.resources, R.drawable.image)
    }
    val blurEvent = SingleLiveEvent<Unit>()
    val realtimeEvent = SingleLiveEvent<Unit>()
    var progressing = MutableLiveData<Boolean>().apply { value = false }

    private val blurEngineMap = HashMap<BlurType, BlurEngine>().apply {
        put(BlurType.BOX_BLUR, BoxBlur())
        put(BlurType.BOX_BLUR_OPTIMIZED, BoxBlurOptimized())
        put(BlurType.GAUSSIAN_BLUR_RS, GaussianBlurRS(application))
        put(BlurType.STACK_BLUR, StackBlur())
    }

    fun getCurrentBlurEngine(): BlurEngine? {
        return blurEngineMap[blurType.value]
    }

    fun setRadius(progress: Int) {
        this.liveRadius.value = progress
    }

    fun getRadius(): Int = this.liveRadius.value!!

    fun onResetClick() {
        bmpImage.value =
            BitmapFactory.decodeResource(getApplication<Application>().resources, R.drawable.image)
    }

    fun onBlurClick() {
        blurEvent.call()
    }
    fun onRealtimeClick(){
        realtimeEvent.call()
    }
}