package com.charlezz.blur

enum class BlurType(val abstractBlur: AbstractBlur){
    BOX_BLUR(BoxBlur()),
    BOX_BLUR_OPTIMIZED(BoxBlurOptimized())
}