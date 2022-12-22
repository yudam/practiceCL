package com.mdy.practicecl.audio

import android.annotation.SuppressLint
import android.app.Application
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioRecord.READ_NON_BLOCKING
import android.media.MediaRecorder
import android.util.Log
import com.mdy.practicecl.App
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread
import kotlin.math.min

/**
 * User: maodayu
 * Date: 2022/10/26
 * Time: 20:12
 */
@SuppressLint("MissingPermission")
class Records {
    
    private val TAG = "Record_Audio"

    private val temp_audio_file = App.getInstance().cacheDir.absolutePath+File.separator+"record_1_audio"

    private var isRecord = false
    private var audioRecord: AudioRecord? = null
    private var callback: AudioFrameCallback? = null

    private val sampleRate = 44100
    private val minBufferSize: Int

    init {
        minBufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT)
        audioRecord = AudioRecord(MediaRecorder.AudioSource.DEFAULT,
            sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize)
        Log.i(TAG, "minBufferSize: "+minBufferSize)
    }


    private val dataQueue = LinkedBlockingQueue<ByteBuffer>()

    private fun executeWriteAudio() {
        thread {
            while (isRecord) {
                val data = ByteBuffer.allocateDirect(minBufferSize)
                when (audioRecord?.read(data, minBufferSize)) {
                    AudioRecord.ERROR_INVALID_OPERATION -> {
                        Log.i(TAG, "视频写入失败。。。。。。")
                    }
                    else -> {
                        Log.i(TAG, "视频写入中。。。。。。" + data.limit())
                        dataQueue.add(data)
                    }
                }
            }
        }
    }


    private fun executeReadAudio() {
        thread {
            while (isRecord) {
                val mediaData = dataQueue.poll()
                if(mediaData != null){
                   // Log.i(TAG, "音频读取    "+mediaData.limit())
                    val packet = MediaPacket().apply {
                        data =  mediaData
                    }
                  //  callback?.frameBuffer(packet)
                }
            }
        }
    }

    fun setCallBack(audioCallback:AudioFrameCallback){
        callback = audioCallback
    }

    fun startRecording() {
        Log.i(TAG, "开始录音    startRecording: ")
        isRecord = true
        audioRecord?.startRecording()
        executeWriteAudio()
        executeReadAudio()
    }


    fun stopRecording() {
        Log.i(TAG, "结束录音    stopRecording")
        isRecord = false
        if(audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING){
            audioRecord?.stop()
            Log.i(TAG, " audioRecord      stop()")
        }
        if(audioRecord?.state == AudioRecord.STATE_INITIALIZED){
            audioRecord?.release()
            Log.i(TAG, " audioRecord      release()")
        }
        callback?.frameEnd()
    }
}