package com.mdy.practicecl.player

/**
 * User: maodayu
 * Date: 2023/3/14
 * Time: 17:54
 * 纹理的渲染位置
 */
class DisplayRect(
    val centerX: Float,
    val centerY: Float,
    val width: Int,
    val height: Int,
    val parentWidth: Int,
    val parentHeight: Int,
) {


    override fun toString(): String {
        return "DisplayRect(centerX=$centerX, centerY=$centerY, width=$width, height=$height, parentWidth=$parentWidth, parentHeight=$parentHeight)"
    }
}