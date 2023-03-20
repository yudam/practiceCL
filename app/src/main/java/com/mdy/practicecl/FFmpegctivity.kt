package com.mdy.practicecl

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import androidx.core.app.ActivityCompat
import com.mdy.practicecl.databinding.ActivityCamearLiveBinding
import com.mdy.practicecl.databinding.ActivityFfmpegctivityBinding

class FFmpegctivity : AppCompatActivity() {

    private lateinit var binding: ActivityFfmpegctivityBinding


    private var mHolder:SurfaceHolder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFfmpegctivityBinding.inflate(layoutInflater)
        setContentView(binding.root)


        mHolder = binding.videoView.holder
        mHolder?.addCallback(object :SurfaceHolder.Callback{
            override fun surfaceCreated(holder: SurfaceHolder) {
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
            }

        })

        binding.btnStartRtmp.setOnClickListener {
            val resList = getVideoFromSDCard()
            val videoPath = resList.find { it.endsWith("tdown.mp4") } ?: resList[0]
            if(resList.isNotEmpty()){
                startRtmp(videoPath,"rtmp://172.16.0.97:1935/live/room")
            }

        }


        binding.btnStopRtmp.setOnClickListener {
            stopRtmp()
        }

        binding.btnStartDecoder.setOnClickListener {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),123)
            } else {
                val videoList =  getVideoFromSDCard()
                if(videoList.isNotEmpty()){
                    startDecoder(videoList[0],mHolder?.surface!!)
                }
            }
        }

    }

    external fun ffmpegCreate(): Boolean

    external fun ffmpegRelease(): Boolean

    external fun startRtmp(url:String,rtmpUrl:String)

    external fun stopRtmp()

    private external fun startDecoder(url:String, surface: Surface)


    private fun getVideoFromSDCard(): List<String> {
        var list = ArrayList<String>(10)
        var projection: Array<String> = arrayOf(MediaStore.Video.Media.DATA)
        var cursor = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null,
            null, null) ?: return emptyList()
        while (cursor.moveToNext()) {
            var path = cursor.getString(cursor
                .getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
            list.add(path)
        }
        cursor.close()
        return list
    }
}