package com.mdy.practicecl

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import com.mdy.practicecl.databinding.ActivityCamearLiveBinding
import com.mdy.practicecl.databinding.ActivityFfmpegctivityBinding

class FFmpegctivity : AppCompatActivity() {

    private lateinit var binding: ActivityFfmpegctivityBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFfmpegctivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStartRtmp.setOnClickListener {
               startRtmp()
//            val videoList = getVideoFromSDCard()
//            Log.i("MDY", "onCreate: ")

        }


        binding.btnStopRtmp.setOnClickListener {
            stopRtmp()
        }

    }

    external fun ffmpegCreate(): Boolean

    external fun ffmpegRelease(): Boolean

    external fun startRtmp()

    external fun stopRtmp()


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