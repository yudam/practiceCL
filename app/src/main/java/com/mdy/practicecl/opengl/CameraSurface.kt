package com.mdy.practicecl.opengl

import android.content.Context
import android.content.res.Resources
import android.graphics.SurfaceTexture
import android.opengl.*
import android.util.AttributeSet
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * User: maodayu
 * Date: 2022/6/20
 * Time: 11:14
 */
class CameraSurface(context: Context?, attrs: AttributeSet?) : GLSurfaceView(context, attrs),
    GLSurfaceView.Renderer {

    /**
     * 传递到Camera中，获取图像数据，刷新到纹理中
     */
    private lateinit var surfaceTexture: SurfaceTexture
    // 纹理ID
    private var textureId: Int = -1
    // 工程ID
    private var programId: Int = -1
    private val vMatrix = FloatArray(16)
    private var vPosition = -1
    private var vTextCoord = -1
    private var uMatrix = -1
    private var uTextureUnit = -1
    //相机
    private val camera1 = Camera1(getContext())


    private val vertexts = floatArrayOf(
        -0.5f, -0.5f, 0f,  // bottom left
        0.5f, -0.5f, 0f,   // bottom right
        -0.5f, 0.5f, 0f,   // top left
        0.5f, 0.5f, 0f     // top right
    )

    /**
     * 纹理坐标，以左上角为原点，区间为[0,1],向右为正S，向下为正T
     *
     * 后置摄像头预览旋转了270度，所以后置摄像头要正旋转90度，才是正常的视角，
     * 前置摄像头预览旋转了90度，所以前置摄像头要逆旋转90度，
     */
    //后置
    private val fragments = floatArrayOf(
        0f, 1f,  // bottom left
        1f, 1f,  // bottom right
        0f, 0f,  // top left
        1f, 0f   // top right
    )

//    //前置
//    private val fragments = floatArrayOf(
//        1f, 0f,
//        0f, 0f,
//        0f, 1f,
//        1f, 1f
//    )

    private val vertextShader = """
        attribute vec4 aPosition;
        attribute vec2 aTextCoord;
        varying vec2 vTextCoord;
        uniform mat4 v_Matrix;
        void main(){
          vTextCoord = aTextCoord;
          gl_Position = v_Matrix * aPosition;
        }
    """.trimIndent()

    private val fragmentShader = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        varying vec2 vTextCoord;
        uniform samplerExternalOES u_TextureUnit;
        void main() {
           gl_FragColor = texture2D(u_TextureUnit, vTextCoord);
        }
    """

    /**
     * 顶点坐标生成的FloatBuffer数组
     */
    private val vertexBuffer = createFloatBuffer(vertexts)


    /**
     * 纹理 坐标生成的FloatBuffer数组
     */
    private var texBuffer = createFloatBuffer(fragments)

    init {
        setEGLContextClientVersion(2)
        setRenderer(this)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.i("MDY", "onSurfaceCreated: ")
        textureId = GlUtils.getTexture(true)
        surfaceTexture = SurfaceTexture(textureId)
        surfaceTexture.setOnFrameAvailableListener {
            //更新最新数据到纹理中
            it.updateTexImage()
            //会重新调用onDrawFrame方法
            requestRender()
        }
        programId = GlUtils.getProgram(vertextShader, fragmentShader)
        loadData()
        camera1.open()
        camera1.setSurfaceTexture(surfaceTexture)
        camera1.startPreview()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val mSize = camera1.getSize()
        matrix(mSize[0], mSize[1], width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        draw()
    }


    /**
     * 创建纹理ID，生成SurfaceTexture
     */
    private fun createSurfaceTexture() {
        textureId = GlUtils.getTexture(true)
        surfaceTexture = SurfaceTexture(textureId)
    }

    /**
     * OpenGL逆时针绘制，最开始的三个顶点要考虑好方向
     * GLES20.GL_TRIANGLE_FAN   前三个顶点逆时针构成第一个三角形
     * GLES20.GL_TRIANGLE_STRIP
     */
    private fun draw() {
        // 1. 清空之前渲染数据
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        //2. 使用创建的Program
        GLES20.glUseProgram(programId)

        //3. 设置矩阵参数
        GLES20.glUniformMatrix4fv(uMatrix, 1, false, vMatrix, 0)

        //4. 激活纹理  这里GL_TEXTURE0  所以后面glUniform1i中参数为0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glUniform1i(uTextureUnit, 0)

        //5. 设置顶点数据，然后绘制四个顶点
        GLES20.glEnableVertexAttribArray(vPosition)
        GLES20.glVertexAttribPointer(vPosition, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glEnableVertexAttribArray(vTextCoord)
        GLES20.glVertexAttribPointer(vTextCoord, 2, GLES20.GL_FLOAT, false, 0, texBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        //6. 解除使用
        GLES20.glDisableVertexAttribArray(vPosition)
        GLES20.glDisableVertexAttribArray(vTextCoord)

    }



    /**
     * 加载着色器中的attribute和uniform属性ID
     */
    private fun loadData() {
        vPosition = GLES20.glGetAttribLocation(programId, "aPosition")
        vTextCoord = GLES20.glGetAttribLocation(programId, "aTextCoord")
        uMatrix = GLES20.glGetUniformLocation(programId, "v_Matrix")
        uTextureUnit = GLES20.glGetUniformLocation(programId, "u_TextureUnit")
       GlUtils. checkGlError("loadData")
    }

    /**
     * 矩阵变换
     * videoWidth、videoHeight 视频数据的预览大小
     * renderWidth、renderHeight 渲染窗口的大小
     */
    fun matrix(videoWidth: Int, videoHeight: Int, renderWidth: Int, renderHeight: Int) {
        Log.i("Camera_GL", "width: "+videoWidth+"  height:"+videoHeight)
        val modelMatrix = FloatArray(16)
        Matrix.setIdentityM(modelMatrix, 0)
        /**
         * 设置模型矩阵的中心点
         */
        Matrix.translateM(modelMatrix,0,renderWidth.toFloat()/2,renderHeight.toFloat()/2,0f)

        /**
         * 由于摄像头数据本身方向问题，需要对顶点位置进行旋转
         * rotateM中最后参数设置xyz任一为1f，表示绕该轴旋转270
         * 后置摄像头默认旋转270，所以继续旋转90
         * 前置摄像头默认旋转90，所以逆时针旋转90，或
         * 者顺时针旋转
         */
        Matrix.rotateM(modelMatrix,0,90f,0F,0F,1F)

        val aspect = videoWidth.toFloat() / videoHeight
        val newWidth = if (aspect > 1f) {
            renderWidth.toFloat()
        } else {
            renderHeight.toFloat() * aspect
        }

        val newHeight = if (aspect > 1f) {
            renderWidth.toFloat() / aspect
        } else {
            renderHeight.toFloat()
        }

        Log.i(
            "Camera_GL", "videoWidth:" + videoWidth + "  videoHeight:" + videoHeight + " \n"
                + " aspect: " + aspect + "  \n"
                + " renderWidth:"+renderWidth+"  renderHeight:"+renderHeight+" \n"
                + " newWidth: " + newWidth + " newHeight: " + newHeight
        )
        /**
         * 设置模型矩阵的大小
         */
        Matrix.scaleM(modelMatrix,0,newWidth,newHeight,1f)

        //设置正交投影矩阵
        val projectMatrix = FloatArray(16)
        Matrix.orthoM(projectMatrix, 0, 0f, renderWidth.toFloat(), 0f, renderHeight.toFloat(), -1f, 1f)

        //合并投影和模型矩阵
        Matrix.multiplyMM(vMatrix,0,projectMatrix,0,modelMatrix,0)

        /**
         *  投影矩阵 ： 将指定的坐标范围[~,~],转化为标准化设备坐标的范围[-1.0,1.0]
         *
         *  裁剪空间坐标映射到屏幕中(viewPortS设置的范围)
         *
         *
         */
    }

    private fun createFloatBuffer(coords: FloatArray): FloatBuffer {
        val bb = ByteBuffer.allocateDirect(coords.size * Float.SIZE_BYTES)
        bb.order(ByteOrder.nativeOrder())
        val fb = bb.asFloatBuffer()
        fb.put(coords)
        fb.position(0)
        return fb
    }

}