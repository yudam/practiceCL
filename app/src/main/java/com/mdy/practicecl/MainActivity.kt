package com.mdy.practicecl

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

                Log.i("AudioRecord", "没有 RECORD_AUDIO 权限")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),
                    123)
            } else {
                Log.i("AudioRecord", "有 RECORD_AUDIO 权限")
                if (records == null) {
                    records = Records()
                }
                if (!isStart) {
                    isStart = true
                    records?.startRecording()
                    records?.setCallBack(AudioPlayer())
                } else {
                    isStart = false
                    records?.stopRecording()
                }
            }
        }
    }

    external fun stringFromJNI(): String


    external fun simpleCl():Int

    external fun testStruct()

    companion object {
        // Used to load the 'practicecl' library on application startup.
        init {
            System.loadLibrary("practicecl")
        }

    }

    /**
     * 虚函数
     * 指针与引用
     * STL
     */
}