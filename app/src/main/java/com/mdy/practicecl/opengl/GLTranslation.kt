package com.mdy.practicecl.opengl

import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.Matrix
import com.mdy.practicecl.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * 转场动画的实现
 * 转行动画的原理在于，通过FBO首先渲染两个不同的画面，根据两个纹理重新渲染到主屏幕上
 */
class GLTranslation() {

    private val vertexCoord = floatArrayOf(
        -1f, 1f, 0f,
        -1f, -1f, 0f,
        1f, -1f, 0f,
        1f, 1f, 0f
    )

    private val fboCoord = floatArrayOf(
        0f, 0f, 0f,
        0f, 1f, 0f,
        1f, 1f, 0f,
        1f, 0f, 0f
    )

    private val fragmentCoord = floatArrayOf(
        0f, 1f, 0f,
        0f, 0f, 0f,
        1f, 0f, 0f,
        1f, 1f, 0f
    )

    private val vertexBuffer = ByteBuffer.allocateDirect(vertexCoord.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer().apply {
            put(vertexCoord)
            position(0)
        }

    private val fboBuffer = ByteBuffer.allocateDirect(fboCoord.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer().apply {
            put(fboCoord)
            position(0)
        }

    private val fragmentBuffer = ByteBuffer.allocateDirect(fragmentCoord.size * Float.SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer().apply {
            put(fragmentCoord)
            position(0)
        }

    private val tProgram: Int
    private val mainProgram: Int
    private val subProgram: Int

    private val mainFrameBuffer: GLFrameBuffer? = null
    private val subFrameBuffer: GLFrameBuffer? = null

    private val defaultMatrix = FloatArray(16).apply {
        Matrix.setIdentityM(this, 0)
    }

    private val uMatrix = FloatArray(16)

    init {
        val vertex = GlUtils.readRawResourse(R.raw.common_vertex_shader)
        val fragment = GlUtils.readRawResourse(R.raw.simple_vertex_shader)
        val common = GlUtils.readRawResourse(R.raw.common_vertex_shader)
        val squeeze_fragment = GlUtils.readRawResourse(R.raw.squeeze_fragment)
        tProgram = GlUtils.getProgram(vertex, squeeze_fragment)
        mainProgram = GlUtils.getProgram(vertex, fragment)
        subProgram = GlUtils.getProgram(vertex, fragment)
    }


    fun draw() {

        // 渲染第一个FBO
        createGLProgram(mainProgram, mainFrameBuffer!!.textureId, -1, fboBuffer, defaultMatrix)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mainFrameBuffer.frameBufferId)
        render()

        // 渲染第二个FBO
        createGLProgram(subProgram, subFrameBuffer!!.textureId, -1, fboBuffer, defaultMatrix)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, subFrameBuffer.frameBufferId)
        render()

        // 渲染转场动画
        createGLProgram(tProgram, mainFrameBuffer.textureId, subFrameBuffer.textureId, fragmentBuffer,
            uMatrix)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        render()
    }

    private fun render() {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)
    }

    private fun createGLProgram(
        program: Int, texture: Int, texture2: Int,
        textureBuffer: FloatBuffer,
        matrix: FloatArray, progress: Float = 0f,
        degree: IntArray = intArrayOf(2),
    ) {
        GLES20.glUseProgram(program)
        val aPosition = GLES20.glGetAttribLocation(program, "a_Position")
        GLES20.glEnableVertexAttribArray(aPosition)
        GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        val aTextureCoord = GLES20.glGetAttribLocation(program, "a_TextureCoord")
        GLES20.glEnableVertexAttribArray(aTextureCoord)
        GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_FLOAT, false, 0, textureBuffer)
        val aMvpMatrix = GLES20.glGetUniformLocation(program, "a_mvpMatrix")
        GLES20.glUniformMatrix4fv(aMvpMatrix, 1, false, matrix, 0)
        //激活第一个纹理区域
        val uTexture = GLES20.glGetUniformLocation(program, "uTexture")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
        GLES20.glUniform1i(uTexture, 0)

        //绑定转场动画中to对应的参数
        if (texture2 != -1) {
            val mProgress = GLES20.glGetUniformLocation(program, "progress")
            GLES20.glUniform1f(mProgress, progress)

            val mDirection = GLES20.glGetUniformLocation(program, "direction")
            GLES20.glUniform2iv(mDirection, 1, degree, 0)
            // 激活第二个纹理区域
            val uTexture2 = GLES20.glGetUniformLocation(program, "uTexture2")
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texture2)
            GLES20.glUniform1i(uTexture2,1)
        }
    }

    private fun initFBO(width: Int, height: Int): GLFrameBuffer {
        // 创建纹理ID
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        //绑定纹理ID
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
        // 设置纹理过滤和边缘的一些条件
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        // 设置绑定的纹理占用内存大小
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE, null)

        // 创建FBO
        val frameBuffers = IntArray(1)
        GLES20.glGenFramebuffers(1, frameBuffers, 0)
        // 绑定FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[0])
        // FBO绑定纹理附件
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
            GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE, textures[0], 0)
        return GLFrameBuffer(frameBuffers[0], textures[0])
    }


    private data class GLFrameBuffer(
        val frameBufferId: Int,
        val textureId: Int,
    )
}