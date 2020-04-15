package com.charlezz.blursample.realtime

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.charlezz.blursample.databinding.ActivityLiveBlurBinding

class LiveBlurActivity : AppCompatActivity() {

    lateinit var binding: ActivityLiveBlurBinding
    private val adapter = LiveBlurAdapter()
    private val layoutManager = LinearLayoutManager(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveBlurBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter
    }
}
