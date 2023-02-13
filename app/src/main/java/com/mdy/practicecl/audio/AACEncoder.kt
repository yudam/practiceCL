package com.mdy.practicecl.audio

import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * AAC编码
 * AudioRecord录制出来的是PCM音频格式，占用空间较大，不适合网络传输，而AAC是比较合适的压缩格式
 * AAC要添加ADTS Header
 */
class AACEncoder(val audioPath: String) : AudioFrameCallback {

    companion object {
        private const val TAG = "AACEncoder"
        private const val TIMEOUT = 10000L
        private const val AAC_MIME_TYPE = "audio/mp4a-latm"
        private const val CSD_INDEX = "csd-0"
        private const val AAC_HEADER_SIZE = 7
    }

    private var aacEncoder: MediaCodec
    private var sampleRate = 44100
    private var channelCount = 2
    private var bitRate = 16000
    private var maxInputSize = 40000
    private var isEncoder = true

    private val mBufferPool = LinkedBlockingQueue<ByteBuffer>()

    private var muxerUtil: MuxerUtil? = null
    private var outFile = BufferedOutputStream(FileOutputStream(audioPath))


    init {
        val audioFormat = MediaFormat.createAudioFormat(AAC_MIME_TYPE, sampleRate, channelCount)
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
        audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, maxInputSize)
        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK,AudioFormat.CHANNEL_IN_STEREO)
        aacEncoder = MediaCodec.createEncoderByType(AAC_MIME_TYPE)
        aacEncoder.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        aacEncoder.start()
    }


    private fun encoderPcmToAac() {
        val bufferInfo = MediaCodec.BufferInfo()
        while (isEncoder) {
            val inputerBufferIndex = aacEncoder.dequeueInputBuffer(TIMEOUT)
            if (inputerBufferIndex >= 0) {
                val dataBuffer = mBufferPool.poll(1000, TimeUnit.MILLISECONDS)
                val inputBuffer = aacEncoder.getInputBuffer(inputerBufferIndex)
                dataBuffer?.let {
                    inputBuffer?.clear()
                    inputBuffer?.put(it)
                    aacEncoder.queueInputBuffer(inputerBufferIndex, 0, dataBuffer.capacity(), 0, 0)
                }
            }

            /**
             * 获取一个输出缓存句柄，-1表示没有可用的
             * bufferInfo参数包含被编码好的数据，包括pts，size，flags
             */
            val outputBufferIndex = aacEncoder.dequeueOutputBuffer(bufferInfo, TIMEOUT)

            if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                //MediaFormat发生改变，通常只会在开始调用一次，可以设置混合器的轨道
//                Log.i(TAG, "编码器Mediaformat发生改变  ")
//                val newFormat = aacEncoder.outputFormat
//                muxerUtil = MuxerUtil(audioPath)
//                muxerUtil?.addTrack(newFormat)

            } else if (outputBufferIndex >= 0) {
                val outputBuffer = aacEncoder.getOutputBuffer(outputBufferIndex)
                if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    Log.i(TAG, "编码结束: ")
                    isEncoder = false
                } else {
                    outputBuffer?.let {
                        val aacData = ByteArray(bufferInfo.size + 7)
                        //获取编码后的AAC数据，并添加头部参数
                        AacUtils.addADTStoPacket(aacData)
                        it.rewind()
                        it.get(aacData,7,bufferInfo.size)
                        it.rewind()
                        //将编码后的音频写入文件
                        writeAudio2File(aacData,outFile)
                    }
                    Log.i(TAG, "AAC音频数据   size: " + bufferInfo.size + "    pts:" + bufferInfo.presentationTimeUs)
                    //muxerUtil?.putStream(outBuffer, bufferInfo)
                }
                aacEncoder.releaseOutputBuffer(outputBufferIndex, false)
            }
        }
        aacEncoder.stop()
        aacEncoder.release()
        Log.i(TAG, "编码结束: end ")
       // muxerUtil?.release()
    }


    override fun startFirstFrame() {
        Log.i(TAG, "startFirstFrame: 开始接收录音数据")
        thread {
            encoderPcmToAac()
        }
    }

    override fun frameBuffer(packet: MediaPacket) {
        // put 将元素插入到队列尾部，空间不足时可等待插入
        mBufferPool.put(packet.data)
        Log.i(TAG, "frameBuffer in : " + mBufferPool.size)
/*      add 在由于容量限制导无法插入时，会抛出 IllegalStateException 异常
        mBufferPool.add(byteBuffer)
        offer 插入元素到队列的尾部，返回插入结果，可以设置超时时间
        mBufferPool.offer(byteBuffer)
        mBufferPool.offer(byteBuffer,1000,TimeUnit.MILLISECONDS)*/
    }

    override fun frameEnd() {
        isEncoder = false
    }



    fun writeAudio2File(byteArray: ByteArray, outPutStream: OutputStream){
        outPutStream.write(byteArray)
        outPutStream.flush()
        Log.d("audio_remain","write success")
    }
}