package com.mdy.practicecl.muxer

import android.media.MediaCodec
import android.media.MediaFormat
import android.media.MediaMuxer
import android.provider.MediaStore.Audio.Media
import android.util.Log
import com.mdy.practicecl.audio.MediaPacket

/**
 *  1. MediaMuxer混音时，需要先写入视频的I帧，在写入音频否则不会生成对应的mp4
 *  文件
 *  2. MediaMuxer混合H264和AAC生成Mp4时，AAC编码后数据不需要添加ADTS头部
 *  3. AAC编码后数据生成aac文件时，需要添加ADTS头部
 */
class MuxerStrategy(val muxerPath: String) : IEncoderDataListener, Thread("MuxerStrategy") {


    companion object {
        private const val TAG = "MuxerStrategy"
    }

    private val videoList = mutableListOf<MediaPacket>()
    private val audioList = mutableListOf<MediaPacket>()

    private var mMuxerImpl: MuxerImpl? = null

    private var videoFormat: MediaFormat? = null
    private var audioFormat: MediaFormat? = null

    private var videoPts: Long = -1

    private var isAddTrack: Boolean = false
    private var isStop: Boolean = false

    private var isWriteVideo: Boolean = false


    override fun notifyAvailableData(packet: MediaPacket) {
        if (packet.medieType) {
            videoList.add(packet)
        } else {
            audioList.add(packet)
        }
    }

    override fun notifyMediaFormat(format: MediaFormat, isVideo: Boolean) {
        if (isVideo) {
            videoFormat = format
        } else {
            audioFormat = format
        }
    }

    override fun run() {
        mMuxerImpl = MuxerImpl(muxerPath)
        while (!isStop) {
            Log.i(TAG, "videoFormat: " + videoFormat + "   audioFormat:" + audioFormat)
            if (!isAddTrack && videoFormat != null && audioFormat != null) {
                Log.i(TAG, "run: isAddTrack    " + isAddTrack)
                mMuxerImpl?.addTrack(videoFormat!!, true)
                mMuxerImpl?.addTrack(audioFormat!!, false)
                isAddTrack = true
            }

            if (isAddTrack) {
                Log.i(TAG, "doWork: ")
                doWork()
            }
        }

        mMuxerImpl?.release()
    }


    private fun doWork() {
        Log.i(TAG, "doWork: ")
        if (videoList.size > 0) {
            val videoPkt = videoList.removeAt(0)
            videoPts = videoPkt.pts
            if (!isWriteVideo) {
                if (videoPkt.info!!.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME) {
                    isWriteVideo = true
                    mMuxerImpl?.putStream(videoPkt)

                    audioList.removeAll {
                        it.pts < videoPts
                    }
                }
            } else {
                mMuxerImpl?.putStream(videoPkt)
            }
        }


        if (isWriteVideo) {
            while (audioList.isNotEmpty()) {
                val audioPkt = audioList[0]
                if (audioPkt.pts <= videoPts) {
                    mMuxerImpl?.putStream(audioPkt)
                    audioList.removeAt(0)
                } else {
                    return
                }
            }
        }
    }


    fun stopMuxer() {
        isStop = true
    }


    class MuxerImpl(val path: String) {

        companion object {
            private const val TAG = "MuxerImpl"
        }


        private var mMediaMuxer: MediaMuxer? = null
        private var mVideoIndex = -1
        private var mAudioIndex = -1

        private var isStart = false

        init {
            mMediaMuxer = MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        }

        fun addTrack(format: MediaFormat, isVideo: Boolean) {
            Log.i(TAG, "addTrack: " + isVideo)
            if (isVideo) {
                mVideoIndex = mMediaMuxer?.addTrack(format) ?: -1
            } else {
                mAudioIndex = mMediaMuxer?.addTrack(format) ?: -1
            }

            if (mVideoIndex != -1 && mAudioIndex != -1) {
                Log.i(TAG, "addTrack:  start")
                mMediaMuxer?.start()
                isStart = true
            }
        }


        fun putStream(packet: MediaPacket) {
            Log.i(TAG, "putStream:  " + packet.medieType +"  pos:"+packet.data!!.position()+"   limit:"+packet.data!!.limit())
            if (packet.medieType) {
                mMediaMuxer?.writeSampleData(mVideoIndex, packet.data!!, packet.info!!)
            } else {
                mMediaMuxer?.writeSampleData(mAudioIndex, packet.data!!, packet.info!!)
            }
        }

        fun release() {
            Log.i(TAG, "release: ")
            mMediaMuxer?.stop()
            mMediaMuxer?.release()
        }
    }
}