package com.mdy.practicecl.muxer

import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Bundle
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import com.mdy.practicecl.audio.AACEncoder
import com.mdy.practicecl.audio.RecordUtil
import com.mdy.practicecl.codec.H264Encoder
import com.mdy.practicecl.databinding.ActivityMuxerBinding
import java.io.File

class MuxerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMuxerBinding


    private var mH264Encoder:H264Encoder? = null
    private var mRecordUtil:RecordUtil? = null
    private var mMuxerStrategy:MuxerStrategy? = null

    private val cameraRender = CameraRender()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMuxerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mH264Encoder = H264Encoder(getVideoPath())
        cameraRender.setH264Encoder(mH264Encoder!!)
        binding.videoTexture1.surfaceTextureListener = cameraRender

        binding.btnStart.setOnClickListener {

            mMuxerStrategy = MuxerStrategy(getMuxerPath())
            mMuxerStrategy?.start()


            val aacEncoder = AACEncoder(getAudioPath())
            aacEncoder.setListener(mMuxerStrategy)
            mRecordUtil = RecordUtil(aacEncoder)
            mRecordUtil?.start()


            mH264Encoder?.setListener(mMuxerStrategy)
            mH264Encoder?.start()

        }


        binding.btnEnd.setOnClickListener {
            mRecordUtil?.stopRecording()
            mH264Encoder?.stopEncoder()
            mMuxerStrategy?.stopMuxer()
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


    private fun getAudioPath(): String {
        val path = cacheDir.absolutePath + "/media_audio"
        val file = File(path)
        if (!file.exists()) {
            file.createNewFile()
        } else {
            file.delete()
            file.createNewFile()
        }
        return path
    }


    private fun getMuxerPath(): String {
        val path = cacheDir.absolutePath + "/media_muxer.mp4"
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
}