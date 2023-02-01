package com.mdy.practicecl

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.mdy.practicecl.audio.AACDecoder
import com.mdy.practicecl.audio.AACEncoder
import com.mdy.practicecl.audio.AudioPlayer
import com.mdy.practicecl.audio.RecordUtil
import com.mdy.practicecl.databinding.ActivityAacBinding
import java.io.File

class AacActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAacBinding

    private var recordUtil: RecordUtil? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAacBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnDecoderPlay.setOnClickListener {
            val filePath = getAudioPath()
            val aacDecoder = AACDecoder(filePath)
            aacDecoder.setAudioCallback(AudioPlayer())
            aacDecoder.aacToPcm()
        }


        binding.btnEncoder.setOnClickListener {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Log.i("AudioRecord", "没有 RECORD_AUDIO 权限")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),
                    123)
            } else {
                Log.i("AudioRecord", "有 RECORD_AUDIO 权限")
                if (recordUtil == null) {
                    val aacEncoder = AACEncoder(getAudioPath())
                    recordUtil = RecordUtil(aacEncoder)
                    recordUtil?.start()
                } else {
                    recordUtil?.stopRecording()
                }
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {

            val aacEncoder = AACEncoder(getAudioPath())
            recordUtil = RecordUtil(aacEncoder)
            recordUtil?.start()
        }
    }


    private fun getAudioPath(): String {
        val path = cacheDir.absolutePath + "/audiofile3.aac"
        val file = File(path)
        if (!file.exists()) {
            file.createNewFile()
        }
        return path
    }
}