package com.mdy.practicecl.opengl

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import com.mdy.practicecl.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * User: maodayu
 * Date: 2023/1/6
 * Time: 15:37
 */
class TextureSurface(context: Context) : GLSurfaceView(context) {

    private var mProgramId: Int = -1
    private var mTextureId: Int = -1
    private var glPosition: Int = -1
    private var glTexturePosition = -1
    private var glTextureUnit = -1

    val renderer = object : Renderer {
        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            val vertext = GlUtils.readRawResourse(R.raw.simple_vertex_shader)
            val fragment = GlUtils.readRawResourse(R.raw.simple_fragment_shader)
            mTextureId = GlUtils.getTexture()
            mProgramId = GlUtils.getProgram(vertext, fragment)
            loadVertextAttr()
            loadImage()
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
        }

        override fun onDrawFrame(gl: GL10?) {
            draw()
        }
    }

    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    private fun loadImage() {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_tt_1)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
    }

    private fun loadVertextAttr() {
        glPosition = GLES20.glGetAttribLocation(mProgramId, "aPosition")
        glTexturePosition = GLES20.glGetAttribLocation(mProgramId, "aTextCoord")
        glTextureUnit = GLES20.glGetUniformLocation(mProgramId, "uTexture")
    }

    private fun draw() {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glUseProgram(mProgramId)
        GLES20.glEnableVertexAttribArray(glPosition)
        GLES20.glEnableVertexAttribArray(glTexturePosition)
        GLES20.glVertexAttribPointer(glPosition, 2, GLES20.GL_FLOAT,
            false, 0, GLDrawableUtils.getByteBuffer(GLDrawableUtils.common_vertext_coord))
        GLES20.glVertexAttribPointer(glTexturePosition, 2, GLES20.GL_FLOAT,
            false, 0, GLDrawableUtils.getByteBuffer(GLDrawableUtils.common_fragment_coord))

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId)
        GLES20.glUniform1i(glTextureUnit, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }
}