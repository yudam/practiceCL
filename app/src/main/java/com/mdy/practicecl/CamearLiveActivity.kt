package com.mdy.practicecl

import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.opengl.EGLSurface
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import android.view.TextureView
import com.mdy.practicecl.databinding.ActivityCamearLiveBinding
import com.mdy.practicecl.opengl.*

/**
 * TextureView目前通过设置自定义创建的SurfaceTexture会一直抛出异常，原因未知
 *
 * 具体TextureView的使用需要通过设置surfaceTextureListener，来获取SurfaceTexture，在设备相机预览才可以
 *
 * 但是如果保存了SurfaceTexture，则可以在对象不为空的情况下再次使用，且不需要设置surfaceTextureListener，但是
 * 同时也不会回调到surfaceTextureListener接口的onSurfaceTextureAvailable和onSurfaceTextureDestroyed方法
 */
class CamearLiveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCamearLiveBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camear_live)
        binding = ActivityCamearLiveBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnStart.setOnClickListener {

        }


      //  binding.tv.surfaceTextureListener = RenderTexture()
    }


    class RenderTexture:TextureView.SurfaceTextureListener{

        private var mCamera:Camera? = null

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {

            mCamera =  Camera.open()
            mCamera?.setPreviewTexture(surface)
            mCamera?.startPreview()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            mCamera?.stopPreview()
            mCamera?.release()
            return true
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        }

    }

}