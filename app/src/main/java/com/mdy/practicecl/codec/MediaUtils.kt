package com.mdy.practicecl.codec

import android.media.MediaCodecList
import android.util.Log
import kotlin.experimental.and

/**
 * User: maodayu
 * Date: 2023/1/12
 * Time: 11:43
 */
object MediaUtils {

    /**
     * 获取手机支持的编解码器
     * 软编码器通常以OMX.google开头
     */

    fun getSupportCodec() {

        val list = MediaCodecList(MediaCodecList.REGULAR_CODECS)

        Log.i("MediaUtils", "Encoder编码器支持格式: ")
        list.codecInfos.forEach {
            if (it.isEncoder) {
                Log.i("MediaUtils", it.name)
            }
        }

        Log.i("MediaUtils", "Decoder解码器支持格式: ")
        list.codecInfos.forEach {
            if (!it.isEncoder) {
                Log.i("MediaUtils", it.name)
            }
        }
    }

}