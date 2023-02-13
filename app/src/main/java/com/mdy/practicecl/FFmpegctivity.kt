package com.mdy.practicecl

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class FFmpegctivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ffmpegctivity)
    }

    external fun ffmpegCreate():Boolean

    external fun ffmpegRelease():Boolean
}