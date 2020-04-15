package com.charlezz.blursample

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.charlezz.blur.BlurType
import com.charlezz.blursample.databinding.ActivityMainBinding
import com.charlezz.blursample.realtime.LiveBlurActivity
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var viewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel =
            ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application)).get(
                MainViewModel::class.java
            )
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.blurEvent.observe(this, Observer {
            viewModel.progressing.value = true
            var bitmap = (binding.image.drawable as BitmapDrawable).bitmap
            thread {
                val processingTime = measureTimeMillis {
                    bitmap =
                        viewModel.getCurrentBlurEngine()?.blur(bitmap, viewModel.liveRadius.value!!)
                    bitmap?.let {
                        viewModel.bmpImage.postValue(it)
                    }
                }
                viewModel.latestMeasureTimeMills.postValue(processingTime)
                viewModel.progressing.postValue(false)
            }

        })

        viewModel.realtimeEvent.observe(this, Observer {
            startActivity(Intent(this, LiveBlurActivity::class.java))
        })

        viewModel.bmpImage.observe(this, Observer {
            binding.image.setImageBitmap(it)
        })

        binding.spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            BlurType.values().map { it.name })

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.blurType.value = BlurType.values()[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }




    }
}
