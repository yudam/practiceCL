package com.mdy.practicecl.opengl

import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.mdy.practicecl.App
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
/**
 * User: maodayu
 * Date: 2023/3/16
 * Time: 15:39
 * 模版测试的基本使用
 */
class StencilDraw {

    fun drawStencil(){

        /**
         * 1. 开启模版测试
         */
        GLES20.glEnable(GLES20.GL_STENCIL_TEST)

        /**
         * 设置模版测试通过或者失败应该采取的行为
         * GLES20.GL_REPLACE表示模版测试通过会将模版值设置为glStencilFunc中设置的值，比如1
         */
        GLES20.glStencilOp(GLES20.GL_KEEP,GLES20.GL_KEEP,GLES20.GL_REPLACE)

        // 清除模版测试，注意一定要在开启模版测试且允许写入的环境下才可以清除
        GLES20.glClear(GLES20.GL_STENCIL_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)

        // 指定什么情况下通过模版测试默认为GL_ALWAYS，表示总是通过
        GLES20.glStencilFunc(GLES30.GL_ALWAYS,1,0xFF)

        // 允许模版缓冲区的写入，0xFF允许写入， 0x00禁止写入
        GLES20.glStencilMask(0xFF)

        /**
         * 2. 正常绘制颜色，图像等数据
         */



        /**
         * GL_NOTEQUAL表示所有模版值为1的片段都不会绘制
         * 后续绘制不为1的数据
         */
        GLES20.glStencilFunc(GLES20.GL_NOTEQUAL,1,0xFF)
        //  3. 禁用模版测试
        GLES20.glStencilMask(0x00)


        /**
         * 3. 再次绘制模版缓存值为不为1的数据
         */



        // 允许写入
        GLES20.glStencilMask(0xFF)

    }
}