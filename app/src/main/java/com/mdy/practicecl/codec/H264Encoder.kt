package com.mdy.practicecl.codec

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import android.view.Surface
import java.nio.ByteBuffer
import kotlin.experimental.and

/**
 * H264编码
 */
class H264Encoder(val ouputPath: String) {

    private val mMimeType = "video/avc"

    private val outPutType = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4

    private val mMediaCodec = MediaCodec.createEncoderByType(mMimeType)

    private val mBufferInfo = MediaCodec.BufferInfo()

    private val mInputSurface: Surface

    private val mMediaMuxer = MediaMuxer(ouputPath, outPutType)

    private var mTrackIndex = -1

    private val timeoutUs = 10000L


    init {
        val mediaFormat = MediaFormat.createVideoFormat(mMimeType, 1080, 1920)
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
        mediaFormat.setInteger(MediaFormat.KEY_BITRATE_MODE,
            MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR)
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
        // H264编码必须设置sps和pps
        val sps = byteArrayOf(0, 0, 0, 1, 103, 66, 0, 41, -115, -115, 64, 80, 30, -48, 15, 8, -124, 83, -128)
        mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(sps))
        val pps = byteArrayOf(0, 0, 0, 1, 104, -54, 67, -56)
        mediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(pps))
        mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mInputSurface = mMediaCodec.createInputSurface()
        mMediaCodec.start()
    }

    /**
     * 返回编码的输入Surface
     */
    fun getInputSurface(): Surface {
        return mInputSurface
    }


    fun startEncoder(){
        onFrame()
    }


    fun release() {
        mMediaCodec.stop()
        mMediaCodec.release()

        mMediaCodec.stop()
        mMediaCodec.release()
    }

    private fun onFrame() {
        while (true) {
            val outPutIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, timeoutUs)

            if(outPutIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                //启动录制功能
                mTrackIndex =  mMediaMuxer.addTrack(mMediaCodec.outputFormat)
                mMediaMuxer.start()

            }else if (outPutIndex >= 0) {
                val byteBuffer = mMediaCodec.getOutputBuffer(outPutIndex)

                //调整byteBuffer的参数符合mBufferInfo（不确定需不需要）
                byteBuffer?.position(mBufferInfo.offset)
                byteBuffer?.limit(mBufferInfo.offset + mBufferInfo.size)
                //写入数据
                mMediaMuxer.writeSampleData(mTrackIndex, byteBuffer!!, mBufferInfo)
                mMediaCodec.releaseOutputBuffer(outPutIndex, false)

                if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    Log.i("H264Encoder", "onFrame: end of stream")
                    break
                }
            }
        }
    }


    /**
     * 检查当前NAL单元的信息
     * 一个NALU单元由NALU Header + NALU 主体组成， NALU Header由一个字节组成，占8位
     * 可以获取后5位查看当前NALU类型
     */
    private fun check(bytes: ByteArray) {
        var index = 4
        if (bytes[2].toInt() == 0x01) {
            index = 3
        }
        val naluType = bytes[index].and(0x1f).toInt()
        when (naluType) {
            7 -> {
                Log.i("H264Encoder", "naluType: SPS")
            }
            8 -> {
                Log.i("H264Encoder", "naluType: PPS")
            }
            5 -> {
                Log.i("H264Encoder", "naluType: IDR")
            }
            else -> {
                Log.i("H264Encoder", "naluType: other")
            }
        }
    }
}