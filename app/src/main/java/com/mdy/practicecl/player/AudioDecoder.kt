package com.mdy.practicecl.player

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import com.mdy.practicecl.audio.AudioFrameCallback
import com.mdy.practicecl.audio.MediaPacket
import java.nio.ByteBuffer

/**
 * 音频解码
 *
 * AudioTrack接收的Bytebuffer必须是position = 0，limit = size的数据
 */
class AudioDecoder(val filePath: String,val listener: AudioFrameCallback):Runnable {

    companion object {
        private const val TIMEOUTUS = 1000 * 10L
        private const val TAG = "AudioDecoder"
    }


    private lateinit var mMediaExtractor: MediaExtractor
    private lateinit var mMediaCodec: MediaCodec
    private var mMediaFormat: MediaFormat? = null
    private var isUnDecoder: Boolean = false

    private fun initConfig() {
        Log.i(TAG, "initConfig: "+filePath)
        mMediaFormat = getMediaFormat() ?: return
        val sampleRate = mMediaFormat?.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channel = mMediaFormat?.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

        Log.i(TAG, "sampleRate: "+sampleRate+"   channel: "+channel)
        val typeMime = mMediaFormat?.getString(MediaFormat.KEY_MIME) ?: ""
        mMediaCodec = MediaCodec.createDecoderByType(typeMime)
        mMediaCodec.configure(mMediaFormat, null, null, 0)
        mMediaCodec.start()
        doDecoderWork()
    }


    private fun getMediaFormat(): MediaFormat? {
        mMediaExtractor = MediaExtractor()
        mMediaExtractor.setDataSource(filePath)
        for (index in 0 until mMediaExtractor.trackCount) {
            val format = mMediaExtractor.getTrackFormat(index)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            if (mime.startsWith("audio/")) {
                mMediaExtractor.selectTrack(index)
                return format
            }
        }
        return null
    }


    override fun run() {
        initConfig()
    }


    private fun doDecoderWork() {

        val mBufferInfo = MediaCodec.BufferInfo()
        while (!isUnDecoder) {
            val inputIndex = mMediaCodec.dequeueInputBuffer(TIMEOUTUS)
            if (inputIndex >= 0) {
                val inputBuffer = mMediaCodec.getInputBuffer(inputIndex) ?: continue
                val ret = mMediaExtractor.readSampleData(inputBuffer, 0)
                if (ret >= 0) {
                    val len = inputBuffer.remaining()
                    val pts = mMediaExtractor.sampleTime
                    mMediaCodec.queueInputBuffer(inputIndex,0,len,pts,0)
                    mMediaExtractor.advance()
                }
            }

            val outputIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo,TIMEOUTUS)
            if(outputIndex >= 0){
                 if(mBufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM){
                     Log.i(TAG, "doDecoderWork: BUFFER_FLAG_END_OF_STREAM")
                } else {
                    // 获取的ByteBuffer遵循position=0，limit = mBufferInfo.size，本身的容量可能很大
                     val outPutBuffer = mMediaCodec.getOutputBuffer(outputIndex)?:continue


                     val cloneBuffer = ByteBuffer.allocateDirect(outPutBuffer.remaining())
                     cloneBuffer.put(outPutBuffer)
                     cloneBuffer.clear()
                     val copy = MediaCodec.BufferInfo()
                     copy.set(mBufferInfo.offset,mBufferInfo.size,mBufferInfo.presentationTimeUs,mBufferInfo.flags)


                     Log.i(TAG, "doDecoderWork: "+cloneBuffer.position()+"   limit:"+cloneBuffer.limit())
                     val packet = MediaPacket().apply {
                         data = cloneBuffer
                         pts = mBufferInfo.presentationTimeUs
                         info = copy
                         medieType = false
                     }
                     listener.frameBuffer(packet)

                }
                mMediaCodec.releaseOutputBuffer(outputIndex,false)
            }
        }
        listener.frameEnd()
        release()
    }


    private fun release(){
        mMediaExtractor.release()
        mMediaCodec.stop()
        mMediaCodec.release()
    }
}