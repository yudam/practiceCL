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
}