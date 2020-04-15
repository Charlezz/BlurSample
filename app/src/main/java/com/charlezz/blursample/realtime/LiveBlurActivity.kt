package com.charlezz.blursample.realtime

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.appcompat.app.AppCompatActivity
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

        binding.dragBtn.setOnTouchListener(touchListener)
    }

    private val touchListener: OnTouchListener = object : OnTouchListener {
        var dx = 0f
        var dy = 0f
        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            val target: View = binding.blurContainer
            if (event.action == MotionEvent.ACTION_DOWN) {
                dx = target.x - event.rawX
                dy = target.y - event.rawY
            } else if (event.action == MotionEvent.ACTION_MOVE) {
                target.x = event.rawX + dx
                target.y = event.rawY + dy
            }
            return true
        }
    }
}
