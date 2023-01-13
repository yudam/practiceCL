package com.mdy.practicecl.codec

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import kotlin.concurrent.thread

/**
 *  H264流解码
 */
class H264Decoder(val surface: Surface, val filePath: String) {

    /**
     *  video/avc 指的就是H264格式
     */
    private val mMimeType = "video/avc"

    private val mMediaCodec = MediaCodec.createDecoderByType(mMimeType)

    private val mBufferInfo = MediaCodec.BufferInfo()

    private val rf = RandomAccessFile(filePath, "r")

    private var isDecoderFinish = false

    private val timeoutUs = 10000L


    init {
        val mediaFormat = MediaFormat.createVideoFormat(mMimeType, 1080, 1920)
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1080 * 1920 * 5)
        mediaFormat.setInteger(MediaFormat.KEY_BITRATE_MODE,
            MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR)

//        val headerSps = byteArrayOf(0, 0, 0, 1, 103, 66, 0, 41, -115, -115, 64, 80,
//            30, -48, 15, 8, -124, 83, -128)
//        val headerPps = byteArrayOf(0, 0, 0, 1, 104, -54, 67, -56)
//        mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(headerSps))
//        mediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(headerPps))
        mMediaCodec.configure(mediaFormat, surface, null, 0)
        mMediaCodec.start()
    }

    fun startH264Decoder(){
        onFrame()
    }


    private var startTime = 0L

    /**
     * 解码
     */
    private fun onFrame() {
        thread {
            startTime = System.currentTimeMillis()
            while (!isDecoderFinish){
                val inputIndex = mMediaCodec.dequeueInputBuffer(timeoutUs)
                if (inputIndex >= 0) {
                    val inputBuffer = mMediaCodec.getInputBuffer(inputIndex)
                    val byteLength = readH264Data(inputBuffer!!)
                    mMediaCodec.queueInputBuffer(inputIndex, 0, byteLength, 0, 0)
                }

                val outPutIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, timeoutUs)
                if (outPutIndex >= 0) {
                    mMediaCodec.releaseOutputBuffer(outPutIndex, true)
                }
            }
        }
    }

    /**
     * 读取H264数据
     */
    private fun readH264Data(byteBuffer: ByteBuffer): Int {
        val nalData = getNAL()
        byteBuffer.clear()
        byteBuffer.put(nalData)
        return nalData.size
    }

    /**
     * 按照NAL单元读取数据
     */
    private fun getNAL(): ByteArray {
        var offsetPos = 0
        val readData = ByteArray(100000)
        rf.read(readData, 0, 4)
        // 找到启动码，获取下一个NALU的下标
        if (findStartCode4(readData, 0)) {
            offsetPos = 4
        } else {
            rf.seek(0)
            rf.read(readData, 0, 3)
            if (findStartCode3(readData, 0)) {
                offsetPos = 3
            }
        }

        var findNalStartCode = false
        // 表示获取的NALU单元数据的大小
        var nextNalStartPos = 0
        var reWind = 0
        //按照字节来读取到缓存字节数组中，直到文件结束或者下一个启动码
        while (!findNalStartCode) {
            if (offsetPos >= readData.size) break
            val hex = rf.read()
            readData[offsetPos++] = hex.toByte()

            //表明文件解析结束
            if (hex == -1) {
                isDecoderFinish = true
                findNalStartCode = true
                nextNalStartPos = offsetPos
            }

            //获取到了下一个启动码，说明本次NALU解析完成了
            if (findStartCode4(readData, offsetPos - 4)) {
                findNalStartCode = true
                reWind = 4
                nextNalStartPos = offsetPos - reWind
            }

            if (findStartCode3(readData, offsetPos - 3)) {
                findNalStartCode = true
                reWind = 3
                nextNalStartPos = offsetPos - reWind
            }
        }

        //拷贝NALU数据到frameData中
        val frameData = ByteArray(nextNalStartPos)
        System.arraycopy(readData, 0, frameData, 0, nextNalStartPos)
        //rf回退reWind个字节，回退到启动码
        val setPos = rf.filePointer - reWind
        rf.seek(setPos)
        Log.i("H264Decoder", "frameData: "+frameData.size)
        return frameData
    }


    /**
     * 根据启动码查找NALU开头
     * H264的启动码：00 00 00 01   或 00 00 01
     */

    private fun findStartCode4(bytes: ByteArray, offset: Int): Boolean {

        if (bytes[offset].toInt() == 0x00
            && bytes[offset + 1].toInt() == 0x00
            && bytes[offset + 2].toInt() == 0x00
            && bytes[offset + 3].toInt() == 0x01
        ) {

            return true

        }
        return false
    }

    private fun findStartCode3(bytes: ByteArray, offset: Int): Boolean {

        if (bytes[offset + 1].toInt() == 0x00
            && bytes[offset + 2].toInt() == 0x00
            && bytes[offset + 3].toInt() == 0x01
        ) {
            return true
        }
        return false
    }
}