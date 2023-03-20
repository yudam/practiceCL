package com.mdy.practicecl.player

import android.opengl.GLES20
import com.mdy.practicecl.R
import com.mdy.practicecl.opengl.GlUtils

/**
 * User: maodayu
 * Date: 2023/3/14
 * Time: 17:49
 */
class OverlayProgram {

    private var program: Int = -1
    private var glPosition: Int = -1
    private var glTexturePosition = -1
    private var glTextureUnit = -1

    init {
        createProgram()
        loadAttrs()
    }


    private fun createProgram() {
        val vertext = GlUtils.readRawResourse(R.raw.simple_vertex_shader)
        val fragment = GlUtils.readRawResourse(R.raw.simple_fragment_shader)
        program = GlUtils.getProgram(vertext, fragment)
    }


    private fun loadAttrs() {
        glPosition = GLES20.glGetAttribLocation(program, "aPosition")
        glTexturePosition = GLES20.glGetAttribLocation(program, "aTextCoord")
        glTextureUnit = GLES20.glGetUniformLocation(program, "uTexture1")
    }

    fun draw(bTextureId: Int) {


    }
}