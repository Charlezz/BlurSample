package com.charlezz.blursample

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.charlezz.blursample.databinding.ActivityMainBinding
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    lateinit var viewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(MainViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.blurEvent.observe(this, Observer{
            viewModel.progressing.value = true
            var bitmap = (binding.image.drawable as BitmapDrawable).bitmap
            thread {
                val processingTime =  measureTimeMillis {
                    bitmap = viewModel.selectedBlurType.value?.abstractBlur?.blur(bitmap, viewModel.liveRadius.value!!)
                    viewModel.bmpImage.postValue(bitmap)
                }
                viewModel.latestMeasureTimeMills.postValue(processingTime)
                viewModel.progressing.postValue(false)
            }

        })

        viewModel.bmpImage.observe(this, Observer{
            binding.image.setImageBitmap(it)
        })
    }
}
