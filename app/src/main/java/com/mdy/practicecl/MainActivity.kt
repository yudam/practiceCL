package com.mdy.practicecl

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.mdy.practicecl.audio.AudioPlayer
import com.mdy.practicecl.audio.Records
import com.mdy.practicecl.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var isStart = false
    private var records: Records? = null

    private var audioPlayer: AudioPlayer? = null

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


        binding.btn.setOnClickListener {

//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
//                != PackageManager.PERMISSION_GRANTED) {
//
//                Log.i("AudioRecord", "没有 RECORD_AUDIO 权限")
//                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),
//                    123)
//            } else {
//                Log.i("AudioRecord", "有 RECORD_AUDIO 权限")
//                if (records == null) {
//                    records = Records()
//                }
//                if (!isStart) {
//                    isStart = true
//                    records?.startRecording()
//                    records?.setCallBack(AudioPlayer())
//                } else {
//                    isStart = false
//                    records?.stopRecording()
//                }
//            }


            val mNativePtr = ffmpeg_init("")
            ffmpeg_prepare(mNativePtr)

        }
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