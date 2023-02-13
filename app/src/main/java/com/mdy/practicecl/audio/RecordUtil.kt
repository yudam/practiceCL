package com.mdy.practicecl.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.mdy.practicecl.App
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

/**
 * AudioRecord录音
 * 注意权限问题
 */
@SuppressLint("MissingPermission")
class RecordUtil(val callback: AudioFrameCallback,val audioPath:String? = null) : Thread("Audio-Record-1") {

    private val TAG = "RecordUtil"
    private var isRecord = false
    private var audioRecord: AudioRecord? = null
    private val sampleRate = 44100
    private var bufferSize: Int = 0
    private var isFirstFrame = true



    override fun run() {
        Log.i(TAG, "开始录音    startRecording: ")
        initAudioRecord()
        isRecord = true
        audioRecord?.startRecording()
        executeWriteAudio()
    }

    private fun initAudioRecord() {
        bufferSize = AudioRecord.getMinBufferSize(sampleRate,
            AudioFormat.CHANNEL_IN_STEREO,
            AudioFormat.ENCODING_PCM_16BIT)
        audioRecord = AudioRecord(MediaRecorder.AudioSource.DEFAULT,
            sampleRate, AudioFormat.CHANNEL_IN_STEREO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize)
    }

    /**
     *  allocateDirect分配一个直接字节缓冲区，用于频繁的操作字节
     *
     *  这里缓冲区的大小推荐是帧大小的倍数，太小会导致播放无声音
     */
    private fun executeWriteAudio() {
        while (isRecord) {
            val buf = ByteBuffer.allocateDirect(1024*6)
            val len = audioRecord?.read(buf, buf.capacity()) ?: 0
            Log.i(TAG, "录音数据写入:  $len")
            if (len > 0 && len <= buf.capacity()) {
                //重置ByteBuffer的position和limit
                buf.position(len)
                buf.flip()

                val mediaData = MediaPacket().apply {
                    data = buf
                }
                if (isFirstFrame) {
                    callback.startFirstFrame()
                    isFirstFrame = false
                }
                callback.frameBuffer(mediaData)
            }
        }
    }


    fun stopRecording() {
        Log.i(TAG, "结束录音    stopRecording")
        isRecord = false
        callback.frameEnd()
        if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord?.stop()
        }
        if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
            audioRecord?.release()
        }
    }
}