package com.mdy.practicecl.codec

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView
import androidx.core.app.ActivityCompat
import com.mdy.practicecl.audio.AACEncoder
import com.mdy.practicecl.audio.MediaPacket
import com.mdy.practicecl.audio.RecordUtil
import com.mdy.practicecl.databinding.ActivityAacBinding
import com.mdy.practicecl.databinding.ActivityH264Binding
import com.mdy.practicecl.opengl.TextureSurface
import java.io.File
import java.nio.ByteBuffer
import kotlin.concurrent.thread

class H264Activity : AppCompatActivity() {

    companion object {
        private const val TAG = "H264Activity"
    }

    private lateinit var binding: ActivityH264Binding

    private var h264Encoder: H264Encoder? = null
    private val cameraRender = CameraRender()
    private var isEncoder: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityH264Binding.inflate(layoutInflater)
        setContentView(binding.root)

        h264Encoder = H264Encoder(getVideoPath())
        h264Encoder?.start()
        cameraRender.setH264Encoder(h264Encoder!!)

        binding.videoTexture1.surfaceTextureListener = cameraRender
        binding.videoTexture2.surfaceTextureListener = EncoderRender()

        binding.btn264Encoder.setOnClickListener {
            if (isEncoder) {
                isEncoder = false
                h264Encoder?.stopEncoder()
            } else {
                isEncoder = true
                h264Encoder?.startEncoder()
            }
        }
    }


    private fun getVideoPath(): String {
        val path = cacheDir.absolutePath + "/media_surface.mp4"
        val file = File(path)
        if (!file.exists()) {
            file.createNewFile()
        } else {
            file.delete()
            file.createNewFile()
        }
        return path
    }

    class CameraRender : TextureView.SurfaceTextureListener {

        private var mCamera: Camera? = null

        private var mH264Encoder: H264Encoder? = null

        fun setH264Encoder(h264Encoder: H264Encoder) {
            mH264Encoder = h264Encoder
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            mCamera = Camera.open(1)
            val parameter = mCamera?.parameters
            // camera1默认返回的就是NV21格式
            parameter?.previewFormat = ImageFormat.NV21
            mCamera?.parameters = parameter

            mCamera?.setPreviewTexture(surface)
            mCamera?.setPreviewCallback { data, camera ->
                data?.let {
                    mH264Encoder?.put(it)
                }
            }
            mCamera?.setDisplayOrientation(90)
            mCamera?.startPreview()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            mCamera?.stopPreview()
            mCamera?.release()
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        }

    }

    class EncoderRender : TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        }

    }

}