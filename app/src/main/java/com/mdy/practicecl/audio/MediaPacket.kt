package com.mdy.practicecl.audio

import android.media.MediaCodec
import android.media.MediaFormat
import java.nio.ByteBuffer

/**
 * User: maodayu
 * Date: 2022/9/28
 * Time: 17:25
 */
class MediaPacket {

    var data: ByteBuffer? = null
    var info: MediaCodec.BufferInfo? = null
    var pts: Long = -1
    var meidaFormat: MediaFormat? = null
    var medieType:Boolean = false
}