package com.mdy.practicecl.codec

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.util.Log
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit


/**
 * 采集相机的YUV数据，然后H264编码混合生成MP4文件
 *
 * 1. 问题： 编码器只支持NV12，相机传递过来的数据是NV21导致dequeueOutputBuffer直接抛出异常，
 *    解决办法就是设置相机的预览数据格式NV21，编码时转化为NV12
 *
 * 2. 问题： 编码器设置的宽高与相机预览数据的宽高不一致导致花屏，
 *    解决办法就是统一预览数据的宽高和编码器的宽高
 *
 * 3. 问题： 编码时queueInputBuffer传入的pts为0，导致视频只有封面
 *    解决办法就是计算pts然后传入
 *
 * 4. 问题： 相机预览的宽高是1920*1080，编码器的宽高是1080*1920，导致的花屏和视频翻转问题
 *
 *    camera1中预览返回的数据是NV21，且数据显示时是偏转的，所以需要将其顺时针旋转270度或者
 *    逆时针旋转90度，然后在转化为NV12才是正常的数据
 *
 */
class H264Encoder(val outputFile: String) : Thread("H264Encoder-Thread") {

    companion object {
        private const val TAG = "H264Encoder"
    }

    private val mMimeType = "video/avc"

    private lateinit var mMediaCodec: MediaCodec

    private val mediaQueue = LinkedBlockingQueue<ByteArray>()

    private var mediaMuxer: MediaMuxer? = null

    private var mTrackIndex: Int = -1

    private val timeoutUs = 10000L

    @Volatile
    private var isEncoder = true

    override fun run() {
        initConfig()
    }


    private fun initConfig() {
        val mediaFormat = MediaFormat.createVideoFormat(mMimeType, 1080, 1920)
        // 选择对应的YUV4颜色格式
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 3000 * 1000)
        mediaFormat.setInteger(MediaFormat.KEY_BITRATE_MODE,
            MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR)
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
        // H264编码必须设置sps和pps
//        val sps = byteArrayOf(0, 0, 0, 1, 103, 66, 0, 41, -115, -115, 64, 80, 30, -48, 15, 8, -124, 83, -128)
//        mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(sps))
//        val pps = byteArrayOf(0, 0, 0, 1, 104, -54, 67, -56)
//        mediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(pps))
        mMediaCodec = MediaCodec.createEncoderByType(mMimeType)
        mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mMediaCodec.start()
        onFrame()
    }

    fun startEncoder() {
        isEncoder = true
    }

    fun stopEncoder() {
        isEncoder = false
    }


    fun put(packet: ByteArray) {
        mediaQueue.offer(packet)
    }

    private fun onFrame() {
        val mBufferInfo = MediaCodec.BufferInfo()
        while (isEncoder) {
            val packet = mediaQueue.poll(500, TimeUnit.MILLISECONDS) ?: continue
            val buffer = NV21ToNV12(rotateYUV420Degree270(packet))
            val inPutIndex = mMediaCodec.dequeueInputBuffer(timeoutUs)
            if (inPutIndex >= 0) {
                val len = buffer.size
                val inputBuffer = mMediaCodec.getInputBuffer(inPutIndex) ?: return
                inputBuffer.clear()
                inputBuffer.put(buffer)
                mMediaCodec.queueInputBuffer(inPutIndex, 0, len, System.nanoTime() / 1000, 0)
            }

            val outputBufferIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, timeoutUs)
            if (outputBufferIndex >= 0) {
                val byteBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex)
                if ((mBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.i(TAG, "BUFFER_FLAG_END_OF_STREAM: ")
                } else if ((mBufferInfo.flags and MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) != 0) {
                    Log.i(TAG, "INFO_OUTPUT_FORMAT_CHANGED: ")
                    mediaMuxer = MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
                    mTrackIndex = mediaMuxer?.addTrack(mMediaCodec.outputFormat) ?: -1
                    mediaMuxer?.start()
                } else if ((mBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // 表示当前缓冲区携带的是编码器的初始化信息，并不是媒体数据
                    Log.i(TAG, "BUFFER_FLAG_CODEC_CONFIG: ")
                } else if ((mBufferInfo.flags and MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0) {
                    // 当前缓冲区是关键帧信息
                    Log.i(TAG, "BUFFER_FLAG_KEY_FRAME: ")
                    byteBuffer?.position(mBufferInfo.offset)
                    byteBuffer?.limit(mBufferInfo.offset + mBufferInfo.size)
                    mediaMuxer?.writeSampleData(mTrackIndex, byteBuffer!!, mBufferInfo)
                    mMediaCodec.releaseOutputBuffer(outputBufferIndex, false)
                } else {
                    byteBuffer?.position(mBufferInfo.offset)
                    byteBuffer?.limit(mBufferInfo.offset + mBufferInfo.size)
                    mediaMuxer?.writeSampleData(mTrackIndex, byteBuffer!!, mBufferInfo)
                    mMediaCodec.releaseOutputBuffer(outputBufferIndex, false)
                }
            }
        }

        mMediaCodec.stop()
        mMediaCodec.release()

        mediaMuxer?.let {
            it.stop()
            it.release()
        }
    }

    /**
     * NV21顺时针旋转270度
     */
    private fun rotateYUV420Degree270(data: ByteArray, imageWidth: Int = 1920, imageHeight: Int = 1080): ByteArray {
        val yuv = ByteArray(imageWidth * imageHeight * 3 / 2)
        // Rotate the Y luma
        var i = 0

        for (x in imageWidth - 1 downTo 0) {
            for (y in 0 until imageHeight) {
                yuv[i] = data[y * imageWidth + x]
                i++
            }
        }
        i = imageWidth * imageHeight
        var x = imageWidth - 1
        while (x > 0) {
            for (y in 0 until imageHeight / 2) {
                yuv[i] = data[(imageWidth * imageHeight + y * imageWidth + (x - 1))]
                i++
                yuv[i] = data[(imageWidth * imageHeight + y * imageWidth + x)]
                i++
            }
            x -= 2
        }
        return yuv
    }


    /**
     * 将YUV420sp的NV21转化为NV12
     */
    private fun NV21ToNV12(nv21: ByteArray, width: Int = 1920, height: Int = 1080): ByteArray {
        val nv12 = ByteArray(width * height * 3 / 2)
        val framesize = width * height
        var i = 0
        var j = 0
        System.arraycopy(nv21, 0, nv12, 0, framesize)
        i = 0
        while (i < framesize) {
            nv12[i] = nv21[i]
            i++
        }
        j = 0
        while (j < framesize / 2) {
            nv12[framesize + j - 1] = nv21[j + framesize]
            j += 2
        }
        j = 0
        while (j < framesize / 2) {
            nv12[framesize + j] = nv21[j + framesize - 1]
            j += 2
        }
        return nv12
    }
}