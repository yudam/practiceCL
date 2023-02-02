package com.mdy.practicecl

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import com.mdy.practicecl.opengl.GLTranslation
import com.mdy.practicecl.opengl.TextureSurface
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class OpenGLActivity : AppCompatActivity() {

    private lateinit var textureView: TextureSurface
    private lateinit var glSurfaceView: GLSurfaceView

    private lateinit var glTranslation: GLTranslation

    private val renderer = object : GLSurfaceView.Renderer {
        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            glTranslation.initPg()
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
            glTranslation.initBuffer(width, height)
        }

        override fun onDrawFrame(gl: GL10?) {
            glTranslation.senDraw {
                glSurfaceView.requestRender()
            }
            glTranslation.draw()

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val glConfig = intent.getIntExtra("glConfig", 0)
        if (glConfig == 0) {
            textureView = TextureSurface(this)
            setContentView(textureView)
        } else {
            textureView = TextureSurface(this)
            glTranslation = GLTranslation(this)
            glSurfaceView = GLSurfaceView((this))
            initGL()
            setContentView(glSurfaceView)
        }
    }

    private fun initGL() {
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(renderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }
}