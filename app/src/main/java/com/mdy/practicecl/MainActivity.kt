package com.mdy.practicecl

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.mdy.practicecl.codec.MediaUtils
import com.mdy.practicecl.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Example of a call to a native method
        binding.sampleText.text = stringFromJNI()


        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE),
            123)


        val holder = binding.surfaceView.holder
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
            }
        })

        binding.btnFormat.setOnClickListener {
            MediaUtils.getSupportCodec()
        }


        binding.btnAac.setOnClickListener {
            start(AacActivity::class.java)
        }

        binding.btnGlImage.setOnClickListener {
            start(OpenGLActivity::class.java,0)
        }

        binding.btnGlTranslation.setOnClickListener {
            start(OpenGLActivity::class.java,1)
        }

        binding.btnGlEgl.setOnClickListener {
            start(EGLActivity::class.java,1)
        }
    }


    private fun start(activity: Class<*>,config:Int = -1) {
        val intent = Intent(this, activity)
        intent.putExtra("glConfig",config)
        startActivity(intent)
    }

    external fun stringFromJNI(): String


    external fun simpleCl(): Int

    external fun testStruct()

    companion object {
        // Used to load the 'practicecl' library on application startup.
        init {
            System.loadLibrary("practicecl")
        }

    }


    external fun ffmpeg_init(url: String): Long

    external fun ffmpeg_prepare(nativePtr: Long)

    external fun ffmpeg_release()
}