package com.mdy.practicecl.audio

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
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


    override fun onLooperPrepared() {
        workHandler = object : Handler(looper) {
            override fun handleMessage(msg: Message) {
                doWork()
            }
        }
    }


    private fun doWork() {
        val currtime = System.nanoTime()

        if (expectTime == -1L) {
            expectTime = currtime
        } else {
            cumulativeTime = abs(expectTime - currtime)
        }

        val offset = cumulativeTime / 1000 / 1000
        if (offset >= 10) {
            Log.i(TAG, "误差超过 ： " + offset)
        }

        if (pts == -1L) {
            pts = System.currentTimeMillis()
        } else {
            pts += per_frame_audio / 1000 / 1000
        }

    }

}