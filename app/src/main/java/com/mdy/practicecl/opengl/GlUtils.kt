package com.mdy.practicecl.opengl

import android.opengl.GLES10
import android.opengl.GLES11Ext
import android.opengl.GLES20
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
        Log.i("glError", "vertexId: "+vertexId+"   fragmentId:"+fragmentId)
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
        val intArray = IntArray(1)
        GLES20.glGenTextures(1, intArray, 0)
        if (isOES) {
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, intArray[0])
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES10.GL_TEXTURE_MIN_FILTER, GLES10.GL_NEAREST)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES10.GL_TEXTURE_MAG_FILTER, GLES10.GL_LINEAR)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES10.GL_TEXTURE_WRAP_S, GLES10.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES10.GL_TEXTURE_WRAP_T, GLES10.GL_CLAMP_TO_EDGE)
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, intArray[0])
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES10.GL_TEXTURE_MIN_FILTER, GLES10.GL_NEAREST)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES10.GL_TEXTURE_MAG_FILTER, GLES10.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES10.GL_TEXTURE_WRAP_S, GLES10.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES10.GL_TEXTURE_WRAP_T, GLES10.GL_CLAMP_TO_EDGE)

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