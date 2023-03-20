package com.mdy.practicecl.codec

/**
 * User: maodayu
 * Date: 2023/3/7
 * Time: 17:33
 */
class Stream(val type:String) {

    companion object{

        val MicStream = Stream("MIC")

        val LocalStream = Stream("local")

        val EmptyStream = Stream("Empty")
    }
}