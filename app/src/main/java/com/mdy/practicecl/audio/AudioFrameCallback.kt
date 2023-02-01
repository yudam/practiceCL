package com.mdy.practicecl.audio

/**
 * User: maodayu
 * Date: 2022/11/11
 * Time: 14:06
 */
interface AudioFrameCallback {

    fun startFirstFrame(){}

    fun frameBuffer(packet: MediaPacket)

    fun frameEnd()
}