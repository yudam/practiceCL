package com.mdy.practicecl.opengl

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.Matrix
import android.os.Handler
import android.os.HandlerThread
import com.mdy.practicecl.R
import java.nio.FloatBuffer

/**
 * 转场动画的实现
 * 转行动画的原理在于，通过FBO首先渲染两个不同的画面，根据两个纹理重新渲染到主屏幕上
 * 通过动画来不断的计算片段重新渲染
 *
 * 遇到问题： 1. 在创建的子线程更新draw方法渲染无效：需要在Render创建的GLThread中才有效
 *          2. FBO和纹理绑定时，注意是GLES20.GL_TEXTURE_2D
 */
class GLTranslation(val context: Context) :HandlerThread("translation-gl-thread") {

    private val vertexBuffer = DrawBuffer.getByteBuffer(DrawBuffer.DrawType.CommonVertex)
    private val fboBuffer = DrawBuffer.getByteBuffer(DrawBuffer.DrawType.OriginalTexture)
    private val fragmentBuffer = DrawBuffer.getByteBuffer(DrawBuffer.DrawType.CommonTexture)
    private var tProgram: Int = -1
    private var mainProgram: Int = -1
    private var subProgram: Int = -1
    private var mainFrameBuffer: GLFrameBuffer? = null
    private var subFrameBuffer: GLFrameBuffer? = null
    private var mTextureId1 = -1
    private var mTextureId2 = -1
    // 片段着色器中需要的转场动画参数
    private var mProgress = 0f
    private var mDuration = 5000L
    private var mDirection = FloatArray(2)
    private var animHandler:Handler? = null


    private var uMatrix = FloatArray(16)

    private var callback:(()->Unit)? =  null

    fun senDraw(cbk:()->Unit){

        callback = cbk
    }

    /**
     * 1. 初始化创建Program
     */
    fun initPg() {
        mDirection[0] = 1f
        mDirection[1] = 0f

        val options1 = BitmapFactory.Options()
        options1.inScaled = false
        val bitmap1 = BitmapFactory.decodeResource(context.resources, R.drawable.ic_1, options1)
        mTextureId1 = GlUtils.getTexture(bitmap1)

        val options2 = BitmapFactory.Options()
        options2.inScaled = false
        val bitmap2 = BitmapFactory.decodeResource(context.resources, R.drawable.ic_2, options2)
        mTextureId2 = GlUtils.getTexture(bitmap2)

        val vertex = GlUtils.readRawResourse(R.raw.simple_vertex_shader)
        val fragment = GlUtils.readRawResourse(R.raw.simple_fragment_shader)
        val mainFragment = GlUtils.readRawResourse(R.raw.simple_zoom_fragment)
        mainProgram = GlUtils.getProgram(vertex, fragment)
        subProgram = GlUtils.getProgram(vertex, fragment)
        tProgram = GlUtils.getProgram(vertex,mainFragment)
        GlUtils.checkGlError("initPg")

        start()

        animHandler = Handler(looper) {
            callback?.invoke()
            true
        }
    }


    /**
     * 预先创建过渡动画需要的连个FBO，缓存起来
     */
    fun initBuffer(width: Int,height: Int){
        mainFrameBuffer =  initFBO(width, height)
        subFrameBuffer = initFBO(width, height)
        val cx = width/2
        val cy = height/2
        val modelMatrix = FloatArray(16).apply {
            Matrix.setIdentityM(this, 0)
        }
        Matrix.translateM(modelMatrix, 0, cx.toFloat(), cy.toFloat(), 0f)
        Matrix.scaleM(modelMatrix, 0, width.toFloat()/2, width.toFloat()/2, 1f)

        val projectMatrix = FloatArray(16).apply {
            Matrix.orthoM(this,0,0f,width.toFloat(),0f,height.toFloat(),-1f,1f)
        }
        Matrix.multiplyMM(uMatrix,0,projectMatrix,0,modelMatrix,0)
    }



    /**
     * 2. 创建FBO，并返回对应的FBO ID
     */
    private  fun initFBO(width: Int, height: Int): GLFrameBuffer {
        // 创建纹理ID
        val textureId = GlUtils.getTexture()
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
            GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureId, 0)
        return GLFrameBuffer(frameBuffers[0], textureId)
    }

    /**
     * 3. 绑定Program ，绘制纹理
     */
    private fun createGLProgram(
        program: Int, texture: Int, texture2: Int,
        textureBuffer: FloatBuffer,
        matrix: FloatArray, progress: Float = 0f,
        degree: FloatArray = FloatArray(2),
    ) {
        GLES20.glUseProgram(program)
        val aPosition = GLES20.glGetAttribLocation(program, "aPosition")
        GLES20.glEnableVertexAttribArray(aPosition)
        GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        val aTextureCoord = GLES20.glGetAttribLocation(program, "aTextCoord")
        GLES20.glEnableVertexAttribArray(aTextureCoord)
        GLES20.glVertexAttribPointer(aTextureCoord, 3, GLES20.GL_FLOAT, false, 0, textureBuffer)
        val aMvpMatrix = GLES20.glGetUniformLocation(program, "aMvpMatrix")
        GLES20.glUniformMatrix4fv(aMvpMatrix, 1, false, matrix, 0)
        //激活第一个纹理区域,并绑定纹理ID（这里的纹理ID是已经更新了图片的纹理ID）
        val uTexture = GLES20.glGetUniformLocation(program, "uTexture1")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
        GLES20.glUniform1i(uTexture, 0)

        //绘制转场动画的fragment参数和纹理
        if (texture2 != -1) {
            val mProgress = GLES20.glGetUniformLocation(program, "progress")
            GLES20.glUniform1f(mProgress, progress)
            val mDirection = GLES20.glGetUniformLocation(program, "direction")
            GLES30.glUniform2fv(mDirection, 1, degree, 0)
            // 激活第二个纹理区域
            val uTexture2 = GLES20.glGetUniformLocation(program, "uTexture2")
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texture2)
            GLES20.glUniform1i(uTexture2,1)
        }
    }

    /**
     * 4. 先绑定FBO，绘制纹理，然后解绑到默认的FBO，在绘制转场动画
     *
     * 注意：draw的调用线程一定要在EGL环境线程内，这里就必须在Render所在的线程GLThread中，否则其他线程中的无法绘制
     */
     fun draw() {
        translationAnimate()

        // 渲染第一个FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mainFrameBuffer!!.frameBufferId)
        createGLProgram(mainProgram, mTextureId1, -1, fboBuffer, uMatrix)
        render()

        // 渲染第二个FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, subFrameBuffer!!.frameBufferId)
        createGLProgram(subProgram, mTextureId2, -1, fboBuffer, uMatrix)
        render()

        // 渲染转场动画
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        createGLProgram(tProgram, mainFrameBuffer!!.textureId, subFrameBuffer!!.textureId, fragmentBuffer,
            uMatrix,mProgress,mDirection)
        render()
        if(mProgress <= 1f){
            animHandler?.sendEmptyMessageDelayed(0x11,50)
        }
    }

    /**
     * 执行绘制
     */
    private fun render() {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)
    }

    /**
     * 计算转场动画的执行进度,每50ms执行一次draw，直到mProgress = 1f
     */
    private var startTime = System.currentTimeMillis()

    private fun translationAnimate() {
        mProgress = (System.currentTimeMillis() - startTime) / mDuration.toFloat()
        if (mProgress >= 1f) {
            startTime = System.currentTimeMillis()
        }
    }


    private data class GLFrameBuffer(
        val frameBufferId: Int,
        val textureId: Int,
    )
}