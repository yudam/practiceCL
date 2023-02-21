package com.mdy.practicecl.muxer

import android.media.MediaFormat
import com.mdy.practicecl.audio.MediaPacket
import java.nio.ByteBuffer

/**
 * User: maodayu
 * Date: 2023/2/20
 * Time: 17:47
 */
interface IEncoderDataListener {

    fun notifyAvailableData(packet: MediaPacket)

    fun notifyMediaFormat(format: MediaFormat,isVideo:Boolean)
}