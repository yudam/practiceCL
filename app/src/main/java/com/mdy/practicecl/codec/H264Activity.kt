package com.mdy.practicecl.codec

import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import com.mdy.practicecl.databinding.ActivityH264Binding
import java.io.File
import kotlin.concurrent.thread

class H264Activity : AppCompatActivity() {

    companion object {
        private const val TAG = "H264Activity"
    }

    private lateinit var binding: ActivityH264Binding
    private var h264Encoder: H264Encoder? = null
    private val cameraRender = CameraRender()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityH264Binding.inflate(layoutInflater)
        setContentView(binding.root)

//
//        h264Encoder = H264Encoder(getVideoPath())
//        h264Encoder?.start()
//        cameraRender.setH264Encoder(h264Encoder!!)
//
//
//
//
//
//        binding.videoTexture1.surfaceTextureListener = cameraRender
//        binding.videoTexture2.surfaceTextureListener = EncoderRender(getVideoPath())

        binding.btn264Encoder.setOnClickListener {


        }

        binding.btn264Decoder.setOnClickListener {
        }

        binding.btnNalu.setOnClickListener {

            Log.i("H264", "onCreate: " + getVideoPath())
            parseH264Stream(getVideoPath())
        }
    }


    override fun onPause() {
        super.onPause()
        h264Encoder?.stopEncoder()
    }


    private fun getVideoPath(): String {
       // val path = cacheDir.absolutePath + "/NewTextFile.txt"
        val path = cacheDir.absolutePath + "/264.h264"
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

    class EncoderRender(val videoPath: String) : TextureView.SurfaceTextureListener {

        private var h264Decoder: H264Decoder? = null

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            thread {
                Log.i("H264Decoder", "onSurfaceTextureAvailable: ")
//                val mSurface = Surface(surface)
//                h264Decoder =  H264Decoder(mSurface,videoPath)
//                h264Decoder?.startH264Decoder()
            }
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        }

    }

    external fun findStartCode(buffer: ByteArray, offset: Int)

    external fun parseH264Stream(url: String)
}