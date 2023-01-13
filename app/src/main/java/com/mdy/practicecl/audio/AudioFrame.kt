package com.mdy.practicecl.audio

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.view.Surface
import android.view.SurfaceView
import android.view.TextureView
import com.mdy.practicecl.App
import java.nio.ByteBuffer

/**
 * User: maodayu
 * Date: 2022/12/19
 * Time: 17:01
 */
class AudioFrame {

    /**
     * bps 码率 = 采样率 * 量化深度 * 通道数    单位bps，可以转化为Mbps
     *
     * 音频的编码格式：PCM(无损压缩)、WAV、MP3AAC、
     *
     *
     *
     *
     */



    private fun mediaNIO(){
       val byteBuffer =  ByteBuffer.allocateDirect(100)

        byteBuffer.mark()

        byteBuffer.position()

        byteBuffer.limit()

        byteBuffer.capacity()
    }


    private fun surface(context: Context,texture:Int) {
        val surfaceView = SurfaceView(context)
        val glSurfaceView = GLSurfaceView(context)


        val textureView = TextureView(context)
        val surfaceTexture = SurfaceTexture(texture)
        surfaceTexture.setOnFrameAvailableListener {
            it.updateTexImage()
        }
        textureView.setSurfaceTexture(surfaceTexture)


        val surface = Surface(textureView.surfaceTexture)
    }


    /**
     * H264压缩技术：压缩后的帧分为I帧、P帧、B帧
     *
     * I  关键帧，不需要参考其他帧，帧内压缩
     * P  向前参考帧，在压缩时，会参考前面的I帧或者P帧，属于帧间压缩
     * B  双向参考帧，在压缩时，即参考前面的I帧或P帧，也参考后面的P帧，属于帧间压缩
     *
     * P或B帧的解码失败，只会影响到当前的图像序列，不会影响之后的
     *
     * 编码时首先生成I帧，画面差异程度小于5%生成B帧（不会马上输出，会暂时在传输缓冲期中等待），
     * 等到画面差异大于30%生成P帧，这时才会通知传输缓冲器里面的B帧一起输出。
     *
     * 码流中帧之间的顺序是错乱的，所以需要根据PTS来来顺序解码播放，每一个帧之间都有分隔符如
     * 0x 00 00 00 01 或者是 0x 00 00 01，但是还需要一些信息才能够解码，如宽高，码率、帧率
     * 编码方式等，所以在每一个I帧之前，都会有SPS和PPS来存放信息
     *
     * GOP表示两个I帧之间的图像序列，在一个图像序列中只有一个I帧
     *
     * DTS解码顺序
     * PTS显示顺序
     */
}