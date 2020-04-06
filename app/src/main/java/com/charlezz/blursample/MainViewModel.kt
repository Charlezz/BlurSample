package com.charlezz.blursample

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.RadioGroup
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.charlezz.blur.BlurType


class MainViewModel(application: Application) : AndroidViewModel(application) {

    val latestMeasureTimeMills = MutableLiveData<Long>().apply { value = 0 }
    val selectedBlurType = MutableLiveData<BlurType>().apply { value = BlurType.BOX_BLUR }
    val liveRadius = MutableLiveData<Int>().apply { value = 5 }
    val bmpImage = MutableLiveData<Bitmap>().apply {
        value = BitmapFactory.decodeResource(application.resources, R.drawable.image)
    }
    val blurEvent = SingleLiveEvent<Unit>()
    var progressing = MutableLiveData<Boolean>().apply { value = false }

    fun onBlurTypeChanged(radioGroup: RadioGroup, id: Int) {
        when (id) {
            R.id.rb_blur1 -> selectedBlurType.value = BlurType.BOX_BLUR
            R.id.rb_blur2 -> selectedBlurType.value = BlurType.BOX_BLUR_OPTIMIZED
        }
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
}