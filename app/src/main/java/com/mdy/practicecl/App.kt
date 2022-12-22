package com.mdy.practicecl

import android.app.Application

/**
 * User: maodayu
 * Date: 2022/12/19
 * Time: 19:30
 */
class App : Application() {

    private val TASK :Int= 3

    override fun onCreate() {
        super.onCreate()
        insatnce = this
    }


    companion object {

        private var insatnce: App? = null

        fun getInstance(): App {
            return insatnce!!
        }
    }
}