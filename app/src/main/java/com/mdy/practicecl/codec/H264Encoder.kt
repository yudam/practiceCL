package com.mdy.practicecl.codec

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import java.nio.ByteBuffer

/**
 * User: maodayu
 * Date: 2023/1/12
 * Time: 15:16
 */
class H264Encoder {

    private val mimeType = "video/avc"

    private val mMediaCodec = MediaCodec.createDecoderByType(mimeType)


    private fun createMediaFormat(): MediaFormat {
        val mediaFormat = MediaFormat.createVideoFormat(mimeType,1920,1080)
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE,30)
        mediaFormat.setInteger(MediaFormat.KEY_BITRATE_MODE,
            MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR)
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,2)

        val sps = byteArrayOf(0, 0, 0, 1, 103, 66, 0, 41, -115, -115, 64, 80, 30, -48, 15, 8, -124, 83, -128)
        mediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(sps))

        val pps = byteArrayOf(0, 0, 0, 1, 104, -54, 67, -56)
        mediaFormat.setByteBuffer("csd-1",ByteBuffer.wrap(pps))

        return mediaFormat
    }
}