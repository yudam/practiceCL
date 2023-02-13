package com.mdy.practicecl

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.Matrix
import android.os.Bundle
import android.util.Log
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import com.mdy.practicecl.databinding.ActivityEglactivityBinding
import com.mdy.practicecl.opengl.EglCore
import com.mdy.practicecl.opengl.GLDrawableUtils
import com.mdy.practicecl.opengl.GlUtils

/**
 * TextureView简单绘制，通过EglCore创建EGL上下文
 *
 */
class EGLActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEglactivityBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEglactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val render = RenderTexture()
        binding.tv.surfaceTextureListener = render
        render.start()

        binding.btnStart.setOnClickListener {


        }

        //主动设置SurfaceTexture，不会回调SurfaceTextureListener的onSurfaceTextureAvailable方法
        //binding.tv.setSurfaceTexture(SurfaceTexture(GlUtils.getTexture()))
    }


    class RenderTexture : TextureView.SurfaceTextureListener, Thread("RenderTexture") {
        private val TAG = "EGLActivity"
        private val mLock = Object()
        private var mSurfaceTexture: SurfaceTexture? = null
        private var mEglCore: EglCore? = null

        private var videoWidth: Int = 0
        private var videoHeight: Int = 0

        private var bitmap: Bitmap? = null
        private var matrix: FloatArray = FloatArray(16)

        private var mProgramId: Int = -1
        private var mTextureId: Int = -1
        private var glPosition: Int = -1
        private var glTexturePosition = -1
        private var glTextureUnit = -1

        override fun run() {
            while (true) {
                synchronized(mLock) {
                    if (mSurfaceTexture == null) {
                        mLock.wait()
                    }
                }

                mEglCore = EglCore()
                val mEglSurface = mEglCore?.createWindowSurface(mSurfaceTexture!!)

                mEglSurface?.let {
                    mEglCore?.makeCurrent(it)
                    draw()
                    mEglCore?.swapBuffers(it)
                    mEglCore?.releaseSurface(it)
                    mEglCore?.release()
                }
            }

        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            Log.i(TAG, "onSurfaceTextureAvailable: "+ currentThread().name)
            synchronized(mLock) {
                mSurfaceTexture = surface
                videoWidth = width
                videoHeight = height
                mLock.notifyAll()
            }
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            Log.i(TAG, "onSurfaceTextureSizeChanged: ")
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            Log.i(TAG, "onSurfaceTextureDestroyed: ")
            mSurfaceTexture = null
            return true
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            Log.i(TAG, "onSurfaceTextureUpdated: "+currentThread().name)
        }


        private fun draw() {
            GLES20.glViewport(0, 0, videoWidth, videoHeight)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            GLES20.glClearColor(0f, 0f, 0f, 1f)
            bitmap = BitmapFactory.decodeResource(App.getInstance().resources, R.drawable.ic_tt_1)
            val vertext = GlUtils.readRawResourse(R.raw.simple_vertex_shader)
            val fragment = GlUtils.readRawResourse(R.raw.simple_fragment_shader)
            mTextureId = GlUtils.getTexture(bitmap)
            mProgramId = GlUtils.getProgram(vertext, fragment)
            loadVertextAttr()
            GLES20.glUseProgram(mProgramId)
            GLES20.glEnableVertexAttribArray(glPosition)
            GLES20.glEnableVertexAttribArray(glTexturePosition)
            GLES20.glVertexAttribPointer(glPosition, 2, GLES20.GL_FLOAT,
                false, 0, GLDrawableUtils.getByteBuffer(GLDrawableUtils.common_vertext_coord))
            GLES20.glVertexAttribPointer(glTexturePosition, 2, GLES20.GL_FLOAT,
                false, 0, GLDrawableUtils.getByteBuffer(GLDrawableUtils.common_fragment_coord))
            val mvpMatrix = GLES20.glGetUniformLocation(mProgramId, "aMvpMatrix")

            setMatrix(videoWidth, videoHeight)
            GLES20.glUniformMatrix4fv(mvpMatrix, 1, false, matrix, 0)

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId)
            GLES20.glUniform1i(glTextureUnit, 0)
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        }


        private fun loadVertextAttr() {
            glPosition = GLES20.glGetAttribLocation(mProgramId, "aPosition")
            glTexturePosition = GLES20.glGetAttribLocation(mProgramId, "aTextCoord")
            glTextureUnit = GLES20.glGetUniformLocation(mProgramId, "uTexture1")
        }

        /**
         * 构建模型矩阵通过平移、缩放来解决纹理图像的拉伸
         * 构建投影矩阵，确定将哪一部分纹理映射到到坐标上
         */
        private fun setMatrix(width: Int, height: Int) {
            val cx = width / 2
            val cy = height / 2
            val modelMatrix = FloatArray(16).apply {
                Matrix.setIdentityM(this, 0)
            }
            Matrix.translateM(modelMatrix, 0, cx.toFloat(), cy.toFloat(), 0f)
            Matrix.scaleM(modelMatrix, 0, width.toFloat() / 2, width.toFloat() / 2, 1f)

            val projectMatrix = FloatArray(16).apply {
                Matrix.orthoM(this, 0, 0f, width.toFloat(), 0f, height.toFloat(), -1f, 1f)
            }
            Matrix.multiplyMM(matrix, 0, projectMatrix, 0, modelMatrix, 0)
        }

    }

}