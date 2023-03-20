package com.mdy.practicecl.player

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.DisplayMetrics
import android.util.Log
import com.mdy.practicecl.audio.AudioFrameCallback
import com.mdy.practicecl.audio.MediaPacket
import com.mdy.practicecl.codec.Stream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingDeque
import kotlin.math.abs

/**
 * User: maodayu
 * Date: 2023/2/3
 * Time: 11:59
 */
class AudioProcess : HandlerThread("audio-process-thread") {

    private val TAG = "AudioProcess"

    private var workHandler: Handler? = null

    private var expectTime: Long = -1L
    private var cumulativeTime: Long = -1L
    private var pts: Long = -1L


    private val per_frame_audio = (1024 / 44100) * 1000 * 1000 * 1000L

    private val mAudioMap = ConcurrentHashMap<Stream, LinkedBlockingDeque<MediaPacket>>()


    private var listener: AudioFrameCallback? = null

    fun addListener(callback: AudioFrameCallback) {
        listener = callback
    }

    @Synchronized
    fun addAudioPacket(packet: MediaPacket) {
        val audioDeque = mAudioMap[packet.stream]
        if (audioDeque == null) {
            mAudioMap[packet.stream] = LinkedBlockingDeque<MediaPacket>()
        }
        mAudioMap[packet.stream]?.add(packet)
    }

    override fun onLooperPrepared() {
        workHandler = object : Handler(looper) {
            override fun handleMessage(msg: Message) {
                doWork()
            }
        }
    }


    private fun doWork() {

        if (pts == -1L) {
            pts = System.currentTimeMillis()
        } else {
            pts += per_frame_audio / 1000 / 1000
        }

        val audioList = getAudioPacket(pts / 1000)

        if (audioList.isNullOrEmpty()) {
            listener?.frameBuffer(MediaPacket())
        }

        if (audioList.size == 1) {
            listener?.frameBuffer(audioList[0])
        }
        workHandler?.sendEmptyMessageDelayed(0x11, per_frame_audio / 1000 / 1000)
    }


    private fun getAudioPacket(pts: Long): List<MediaPacket> {
        val result = mutableListOf<MediaPacket>()
        mAudioMap.values.forEach {
            while (true) {
                if (checkTmp(pts, it.first) == 0) {
                    result.add(it.removeFirst())
                    break
                } else if (checkTmp(pts, it.first) == 1) {
                    it.removeFirst()
                } else {
                    break
                }
            }
        }
        return result
    }

    private fun checkTmp(pts: Long, packet: MediaPacket): Int {

        val diff = pts - packet.pts

        return if (abs(diff) < 50) {
            0
        } else if (diff > 0) {
            1
        } else {
            2
        }
    }
}