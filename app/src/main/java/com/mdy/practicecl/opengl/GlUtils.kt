package com.mdy.practicecl.opengl

import android.graphics.Bitmap
import android.opengl.GLES10
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import com.mdy.practicecl.App
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder

/**
 * User: maodayu
 * Date: 2023/1/5
 * Time: 19:58
 */
object GlUtils {

    /**
     * 加载着色器
     */
    fun loadShaders(type: Int, glsl: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, glsl)
        GLES20.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.i("MDY", "loadShaders failed: " + GLES20.glGetShaderInfoLog(shader))
        }
        checkGlError("loadShaders")
        return shader
    }


    /**
     * 创建工程
     */
    fun getProgram(vertextShader: String, fragmentShader: String): Int {
        val programId = GLES20.glCreateProgram()
        val vertexId = loadShaders(GLES20.GL_VERTEX_SHADER, vertextShader)
        val fragmentId = loadShaders(GLES20.GL_FRAGMENT_SHADER, fragmentShader)
        Log.i("glError", "vertexId: " + vertexId + "   fragmentId:" + fragmentId)
        GLES20.glAttachShader(programId, vertexId)
        GLES20.glAttachShader(programId, fragmentId)
        GLES20.glLinkProgram(programId)
        checkGlError("glLinkProgram")
        val linked = IntArray(1)
        GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linked, 0)
        if (linked[0] == 0) {
            Log.i("MDY", "loadProgram: link failed")
        }
        checkGlError("getProgram")
        return programId
    }

    /**
     * 创建纹理ID
     */
    fun getTexture(isOES: Boolean = false): Int {
        return getTexture(null,isOES)
    }

    /**
     * glTexParameteri：表示对纹理的设置
     *
     * 当纹理大小和渲染的屏幕大小不一致时处理办法,
     * @see GLES20.GL_TEXTURE_MIN_FILTER 纹理大于屏幕时
     * @see GLES20.GL_TEXTURE_MAG_FILTER 纹理小于屏幕时
     *
     * @see GLES20.GL_LINEAR 使用纹理中最接近的一个像素的颜色作为需要绘制的像素颜色
     * @see GLES20.GL_NEAREST 使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色。

     * 纹理坐标系被称为ST坐标系，S对应x，T对应y，GL_TEXTURE_WRAP_S和GL_TEXTURE_WRAP_T表示超出范围的纹理的处理方式
     * @see GLES20.GL_CLAMP_TO_EDGE 采样纹理边缘，通过边缘补充剩余部分
     * @see GLES20.GL_REPEAT 重复纹理
     * @see GLES20.GL_MIRRORED_REPEAT 镜像重复
     */
    fun getTexture(bitmap: Bitmap? = null, isOES: Boolean = false): Int {
        val intArray = IntArray(1)
        GLES20.glGenTextures(1, intArray, 0)
        if (isOES) {
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, intArray[0])
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES10.GL_TEXTURE_MIN_FILTER, GLES10.GL_NEAREST)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES10.GL_TEXTURE_MAG_FILTER, GLES10.GL_LINEAR)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES10.GL_TEXTURE_WRAP_S, GLES10.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES10.GL_TEXTURE_WRAP_T, GLES10.GL_CLAMP_TO_EDGE)
            bitmap?.let {
                GLUtils.texImage2D(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,0,it,0)
            }
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, intArray[0])
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES10.GL_TEXTURE_MIN_FILTER, GLES10.GL_NEAREST)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES10.GL_TEXTURE_MAG_FILTER, GLES10.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES10.GL_TEXTURE_WRAP_S, GLES10.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES10.GL_TEXTURE_WRAP_T, GLES10.GL_CLAMP_TO_EDGE)
            bitmap?.let {
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,it,0)
            }
        }
        checkGlError("getTexture")
        return intArray[0]
    }


    fun checkGlError(op: String) {
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            val msg = op + ": glError 0x" + Integer.toHexString(error)
            Log.e("MDY", msg)
            throw RuntimeException(msg)
        }
    }


    /**
     *
     */
    fun readRawResourse(id: Int): String {
        val inputStream = App.getInstance().resources.openRawResource(id)
        val reader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(reader)
        val builder = StringBuilder()
        try {
            var nextLine: String?
            while (bufferedReader.readLine().also { nextLine = it } != null) {
                builder.append(nextLine)
                builder.append("\n")
            }
        } catch (e: Exception) {
        }
        return builder.toString()
    }
}