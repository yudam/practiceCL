package com.mdy.practicecl.opengl

import android.opengl.*
import android.util.Log
import android.view.Surface

/**
 *  EGL作为平台和OpenGL ES之间的中间件，使得OpenGL ES能够绘制到当前平台
 */
class EGL {

    private var mEGLDisplay: EGLDisplay
    private var mEGLConfig: EGLConfig?
    private var mEGLContext: EGLContext

    init {
        /**
         * 1. 创建与本地窗口系统之间的连接，返回的EGLDisplay对象，可以抽象理解为设备的显示设备
         */
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            Log.i("EGL", "创建EGLDisplay失败")
        }

        /**
         * 2. 初始化，可以获取EGL的主次版本号
         */
        val version = IntArray(2)
        EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)

        /**
         * 3. 选择渲染表面的配置
         */
        // 1. 定义EGLConfig配置
        val mEGLConfigAttr = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_DEPTH_SIZE, 16,
            EGL14.EGL_STENCIL_SIZE, 8,
            EGL14.EGL_NONE
        )
        // 2. 所有符合配置的EGLConfig
        val configs = arrayOfNulls<EGLConfig>(1)
        // 3. 所有符合的EGLConfig个数
        val numConfigs = IntArray(1)
        if (!EGL14.eglChooseConfig(mEGLDisplay, mEGLConfigAttr, 0, configs, 0, configs.size, numConfigs, 0)) {
            Log.i("EGL", "EGLConfig选择配置失败")
        }
        mEGLConfig = configs[0]

        /**
         * 4. 有了EGLDisplay和EGLConfig就可以配置渲染表面EGLSurface和渲染上下文EGLContext
         */
        mEGLContext = EGL14.EGL_NO_CONTEXT
        //创建上下文环境时，需要创建属性信息用于指定OpenGL使用版本
        val mEGLContextAttr = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE
        )

        // 可以传入一个已经创建的EglContext，会返回一个共享的上下文
        mEGLContext = EGL14.eglCreateContext(mEGLDisplay, mEGLConfig, mEGLContext, mEGLContextAttr, 0)

    }

    /**
     * 5. 创建渲染表面
     * EGL提供了两种创建渲染表面的方式，一种是可见的，渲染到屏幕上，一种是不可见的也即是离屏渲染
     */
    private fun createPbufferSurface(width: Int, height: Int): EGLSurface {
        val surfaceAttr = intArrayOf(
            EGL14.EGL_WIDTH, width, EGL14.EGL_HEIGHT, height, EGL14.EGL_NONE
        )
        val mEGLSurface = EGL14.eglCreatePbufferSurface(mEGLDisplay, mEGLConfig, surfaceAttr, 0)
        return mEGLSurface
    }

    private fun createWindowSurface(surface: Any): EGLSurface {
        val surfaceAttr = intArrayOf(EGL14.EGL_NONE)
        val mEGLSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, surface, surfaceAttr, 0)
        return mEGLSurface
    }
    /**
     * 6. 绑定上下文环境
     * 绑定上下文环境后，就可以执行具体的绘制操作，调用OpenGL相关的方法绘制图形
     *
     * 注意：必须在切换到当前上下文后，才可以执行OpenGL的函数
     */
    private fun makeCurrent(eglSurface: EGLSurface) {
        EGL14.eglMakeCurrent(mEGLDisplay, eglSurface, eglSurface, mEGLContext)
    }

    /**
     * 7. 交换缓冲
     * 绘制结束后，通过交换缓冲将绘制结果显示到屏幕上
     */
    private fun swapBuffers(eglSurface: EGLSurface){
       if(!EGL14.eglSwapBuffers(mEGLDisplay,eglSurface)){
           Log.i("EGL", "swapBuffers 错误")
       }
    }

    /**
     * 8. 释放操作
     * 绘制结束时，进行的释放操作，将对象释放掉
     */
    private fun release(eglSurface: EGLSurface){
        EGL14.eglMakeCurrent(mEGLDisplay,EGL14.EGL_NO_SURFACE,EGL14.EGL_NO_SURFACE,EGL14.EGL_NO_CONTEXT)
        EGL14.eglDestroySurface(mEGLDisplay,eglSurface)
        EGL14.eglDestroyContext(mEGLDisplay,mEGLContext)
        EGL14.eglReleaseThread()
        EGL14.eglTerminate(mEGLDisplay)

        mEGLDisplay = EGL14.EGL_NO_DISPLAY
        mEGLContext = EGL14.EGL_NO_CONTEXT
        mEGLConfig = null
    }

}
