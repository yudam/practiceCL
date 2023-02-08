package com.mdy.practicecl.opengl

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


/**
 *  管理坐标的类
 */
object DrawBuffer {

    private const val PER_COORD_SIZE = 12

    /**
     * 顶点坐标 ，区域为[-1,1],原点位于中心点（0，0）
     * 左上，左下，右下，右上
     */
    private val commonVertexCoord = floatArrayOf(
        -1f, 1f, 0f,
        -1f, -1f, 0f,
        1f, -1f, 0f,
        1f, 1f, 0f
    )

    /**
     * 微纹理坐标，区域为[0,1]，原点位于左下角
     * 由于纹理坐标是翻转了189度，所以要正常显示纹理必须将坐标也翻转180度
     */
    private val commonTextureCoord = floatArrayOf(
        0f, 0f, 0f,
        0f, 1f, 0f,
        1f, 1f, 0f,
        1f, 0f, 0f
    )

    /**
     * 未翻转180度的纹理坐标，也就是纹理是倒着的
     */
    private val originalTextureCoord = floatArrayOf(
        0f, 1f, 0f,
        0f, 0f, 0f,
        1f, 0f, 0f,
        1f, 1f, 0f
    )

    enum class DrawType {
        CommonVertex, CommonTexture, OriginalTexture

    }


    fun getByteBuffer(drawType: DrawType): FloatBuffer {

       return when (drawType) {
            DrawType.CommonVertex -> {
                ByteBuffer.allocateDirect(PER_COORD_SIZE * Float.SIZE_BYTES)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer().apply {
                        put(commonVertexCoord)
                        position(0)
                    }
            }
            DrawType.CommonTexture -> {
                ByteBuffer.allocateDirect(PER_COORD_SIZE * Float.SIZE_BYTES)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer().apply {
                        put(commonTextureCoord)
                        position(0)
                    }
            }
            DrawType.OriginalTexture -> {
                ByteBuffer.allocateDirect(PER_COORD_SIZE * Float.SIZE_BYTES)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer().apply {
                        put(originalTextureCoord)
                        position(0)
                    }
            }
        }
    }

}