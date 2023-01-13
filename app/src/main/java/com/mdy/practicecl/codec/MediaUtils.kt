package com.mdy.practicecl.codec

import android.media.MediaCodecList
import android.util.Log
import java.io.FileInputStream

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


    /**
     *  H264编码：
     *    NAL层（视频数据网络抽象层）：将一帧拆分成多个包进行传输，所有的拆包和组包都是通过NAL层处理
     *    VCL层（视频数据编码层）：对视频原始数据进行压缩
     *
     * H264码流结构：
     *    SODB（原始数据比特流）：VCL层产生的数据流
     *    RBSP（编码后的数据流）：
     *    EBSP（）： 生成编码后的数据流后需要人为在每一帧之前加一个起始位
     *    NALU： NALU头部 + NALU主体
     *
     *
     *
     *     NALU头部
     */
}