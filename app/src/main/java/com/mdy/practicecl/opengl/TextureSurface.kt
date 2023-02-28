package com.mdy.practicecl.opengl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log
import com.mdy.practicecl.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 简单的GlSurfaceView绘制一张图片
 */
class TextureSurface(context: Context) : GLSurfaceView(context) {

    private var mProgramId: Int = -1
    private var mTextureId: Int = -1
    private var glPosition: Int = -1
    private var glTexturePosition = -1
    private var glTextureUnit = -1

    private var bitmap: Bitmap? = null
    private var matrix: FloatArray = FloatArray(16)

    val renderer = object : Renderer {
        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_2)
            val vertext = GlUtils.readRawResourse(R.raw.simple_vertex_shader)
            val fragment = GlUtils.readRawResourse(R.raw.simple_fragment_shader)
            mTextureId = GlUtils.getTexture(bitmap)
            mProgramId = GlUtils.getProgram(vertext, fragment)
            loadVertextAttr()
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
            val cx = width/2
            val cy = height/2
            val modelMatrix = FloatArray(16).apply {
                Matrix.setIdentityM(this, 0)
            }
            Matrix.translateM(modelMatrix, 0, cx.toFloat(), cy.toFloat(), 0f)
            Matrix.scaleM(modelMatrix, 0, 2f, 2f, 1f)

            val projectMatrix = FloatArray(16).apply {
                Matrix.orthoM(this,0,cx.toFloat()-1f,cx.toFloat()+1f,cy.toFloat()-1,cy.toFloat()+1,-1f,1f)
            }
            Log.i("MDY", "projectMatrix: "+projectMatrix.toList().toString())
            Matrix.multiplyMM(matrix, 0, projectMatrix, 0, modelMatrix, 0)
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

    private fun loadVertextAttr() {
        glPosition = GLES20.glGetAttribLocation(mProgramId, "aPosition")
        glTexturePosition = GLES20.glGetAttribLocation(mProgramId, "aTextCoord")
        glTextureUnit = GLES20.glGetUniformLocation(mProgramId, "uTexture1")
    }

    private fun draw() {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glUseProgram(mProgramId)
        GLES20.glEnableVertexAttribArray(glPosition)
        GLES20.glEnableVertexAttribArray(glTexturePosition)
        GLES20.glVertexAttribPointer(glPosition, 2, GLES20.GL_FLOAT,
            false, 0, GLDrawableUtils.getByteBuffer(GLDrawableUtils.common_vertext_coord_full))
        GLES20.glVertexAttribPointer(glTexturePosition, 2, GLES20.GL_FLOAT,
            false, 0, GLDrawableUtils.getByteBuffer(GLDrawableUtils.common_fragment_coord))

        val mvpMatrix = GLES20.glGetUniformLocation(mProgramId, "aMvpMatrix")


        Log.i("MDY", "draw: " + matrix!!.toList().toString())
        GLES20.glUniformMatrix4fv(mvpMatrix, 1, false, matrix, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId)
        GLES20.glUniform1i(glTextureUnit, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
    }
}