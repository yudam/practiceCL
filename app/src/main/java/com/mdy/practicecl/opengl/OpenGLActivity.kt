package com.mdy.practicecl.opengl

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class OpenGLActivity : AppCompatActivity() {

    private lateinit var textureView:TextureSurface


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textureView = TextureSurface(this)
        setContentView(textureView)
    }
}