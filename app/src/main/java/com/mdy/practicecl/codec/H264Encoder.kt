package com.mdy.practicecl.codec

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.opengl.GLES20
import android.util.Log
import com.mdy.practicecl.Utils
import com.mdy.practicecl.audio.MediaPacket
import com.mdy.practicecl.muxer.IEncoderDataListener
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit


/**
 * 采集相机的YUV数据，然后H264编码混合生成MP4文件，在创建MediaFormat时不需要配置sps和pps，在编码接收到
 * 第一帧也就是INFO_OUTPUT_FORMAT_CHANGED时，可以获取outputFormat来获取sps和pps，sps和pps一般位于
 * 码流的起始位置，也就是编码器的第一帧。
 *
 * sps ：序列参数集，保存了和编码序列相关的参数，如编码的profile、level、图像宽高等
 * pps ：图像参数集，保存了图像的相关参数
 *
 * 1. 问题： 编码器只支持NV12，相机传递过来的数据是NV21导致dequeueOutputBuffer直接抛出异常，
 *    解决办法就是设置相机的预览数据格式NV21，编码时转化为NV12
 *
 * 2. 问题： 编码时queueInputBuffer传入的pts为0，导致视频只有封面
 *    解决办法就是计算pts然后传入
 *
 * 3. 问题： 相机预览的宽高是1920*1080，编码器的宽高是1080*1920，导致的花屏和视频翻转问题
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

    private var dataListener:IEncoderDataListener? = null

    @Volatile
    private var isEncoder = true

    override fun run() {
        initConfig()
    }


    /**
     * 对于视频编码来说设置MediaFormat时主要有以下参数：颜色格式、码率、码率控制模式、帧率、I帧间隔
     */
    private fun initConfig(width :Int = 1920,height:Int = 1080) {
        val mediaFormat = MediaFormat.createVideoFormat(mMimeType, width, height)
        // 选择对应的YUV4颜色格式
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE,width * height * 5)
        mediaFormat.setInteger(MediaFormat.KEY_BITRATE_MODE,
            MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR)
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
        mMediaCodec = MediaCodec.createEncoderByType(mMimeType)
        mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mMediaCodec.start()
        onFrame()
    }

    /**
     * 异步加载
     */
    private fun setCallback(){
        mMediaCodec.setCallback(object :MediaCodec.Callback(){
            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
               val inputBuffer =  mMediaCodec.getInputBuffer(index)
            }

            override fun onOutputBufferAvailable(codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo) {
               val outPutBuffer =  mMediaCodec.getOutputBuffer(index)
            }

            override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
            }

        })
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

    fun setListener(listener: IEncoderDataListener?){
        dataListener = listener
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

                    dataListener?.notifyMediaFormat(mMediaCodec.outputFormat,true)
                    // 获取sps和pps
                    val sps = mMediaCodec.outputFormat.getByteBuffer("csd-0")
                    val pps = mMediaCodec.outputFormat.getByteBuffer("csd-1")
                    Log.i(TAG, "sps: "+ Utils.bytesToHex(sps?.array()))
                    sps?.array()?.forEachIndexed { index, byte ->
                        Log.i(TAG, "index: "+index+"   byte: "+byte)
                    }
                    Log.i(TAG, "pps: "+Utils.bytesToHex(pps?.array()))
                    pps?.array()?.forEachIndexed { index, byte ->
                        Log.i(TAG, "index: "+index+"   byte: "+byte)
                    }

                } else if ((mBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // 表示当前缓冲区携带的是编码器的初始化信息，并不是媒体数据
                    Log.i(TAG, "BUFFER_FLAG_CODEC_CONFIG: ")
                } else{
                    // 当前缓冲区是关键帧信息
                    if((mBufferInfo.flags and MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0){
                        Log.i(TAG, "BUFFER_FLAG_KEY_FRAME: ")
                    }
                    val videoArray = ByteArray(mBufferInfo.size)
                    byteBuffer?.let {
                        it.position(mBufferInfo.offset)
                        it.limit(mBufferInfo.offset + mBufferInfo.size)
                        it.get(videoArray,mBufferInfo.offset,mBufferInfo.size)
                    }
                    mediaMuxer?.writeSampleData(mTrackIndex, byteBuffer!!, mBufferInfo)
                    mMediaCodec.releaseOutputBuffer(outputBufferIndex, false)

                    val copy = MediaCodec.BufferInfo()
                    copy.set(mBufferInfo.offset,mBufferInfo.size,
                        mBufferInfo.presentationTimeUs,mBufferInfo.flags)
                    val pkt = MediaPacket().apply {
                        info = copy
                        data = ByteBuffer.wrap(videoArray)
                        medieType = true
                        pts = info!!.presentationTimeUs
                    }
                    dataListener?.notifyAvailableData(pkt)
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