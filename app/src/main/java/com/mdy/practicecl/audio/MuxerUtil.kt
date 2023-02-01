package com.mdy.practicecl.audio

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import java.nio.ByteBuffer

/**
 * 音频混合器
 *
 * MediaMuxer当前只支持AAC压缩格式的音频
 */
class MuxerUtil(val path: String) {

    private lateinit var mMuxer: MediaMuxer

    private var audioTrackIndex: Int = 0

    init {
        val filePath = path + ".mp4"
        mMuxer = MediaMuxer(filePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    }


    fun addTrack(mediaFormat: MediaFormat) {
        Log.i("MuxerUtil", "addTrack: ")
        audioTrackIndex = mMuxer.addTrack(mediaFormat)
        mMuxer.start()
    }


    fun putStream(dataBuffer: ByteBuffer,bufferInfo:MediaCodec.BufferInfo) {
        Log.i("MuxerUtil", "putStream: "+dataBuffer.remaining()+"   size:"+dataBuffer.capacity())
        mMuxer.writeSampleData(audioTrackIndex,dataBuffer,bufferInfo)
    }


    fun release(){
        Log.i("MuxerUtil", "release: ")
        mMuxer.stop()
        mMuxer.release()
    }
}