package com.mdy.practicecl.audio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import java.nio.ByteBuffer

/**
 * User: maodayu
 * Date: 2022/9/19
 * Time: 20:33
 * 音频播放，高级音频技术在于混音、音频增益
 */
class AudioPlayer :AudioFrameCallback{

    private val TAG = "AudioPlayer"

    /**
     * 采样率
     */
    private val sampleRate = 48000

    /**
     * 通道数
     */
    private val channelConfig = 2

    /**
     * 采样深度
     */
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var audioTrack: AudioTrack? = null


    init {
        val mMinBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        Log.i(TAG, "mMinBufferSize:$mMinBufferSize")
        audioTrack = AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
            getChannelConfig(), audioFormat, mMinBufferSize, AudioTrack.MODE_STREAM
        )
        audioTrack?.play()
    }

    private fun getChannelConfig(): Int {
        return if (channelConfig == 1) {
            AudioFormat.CHANNEL_OUT_MONO //单声道
        } else {
            AudioFormat.CHANNEL_OUT_STEREO //双声道
        }
    }


    /**
     *  写入的字节大小默认为一个音频帧的大小，也就是4096
     *  建议为音频帧大小的倍数
     */
    private fun write(audioData: ByteBuffer) {
        val buffSize = if (audioData.remaining() > 4096) {
            audioData.remaining()
        } else {
            4096
        }

        Log.i(TAG, "write: "+audioData.remaining())
        audioTrack?.write(audioData, buffSize, AudioTrack.WRITE_BLOCKING)
    }

    override fun frameBuffer(packet: MediaPacket) {
        packet.data?.let {
            write(it)
        }
    }

    override fun frameEnd() {
        audioTrack?.stop()
        audioTrack?.release()
    }

}