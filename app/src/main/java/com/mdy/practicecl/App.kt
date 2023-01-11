package com.mdy.practicecl

import android.app.Activity
import android.app.Application
import java.lang.reflect.Proxy

/**
 * User: maodayu
 * Date: 2022/12/19
 * Time: 19:30
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        insatnce = this
    }

    fun leakMemory(){

        /**
         * 通过动态代理+Kotlin的委托模式，来动态的实现一个 ActivityLifecycleCallbacks对象
         */
        val lifecycle = ActivityLifecycleCallbacks::class.java
        val delegate = Proxy.newProxyInstance(lifecycle.classLoader, arrayOf(lifecycle)) { proxy, method, args ->
        } as ActivityLifecycleCallbacks
        val lifecycleCallback = object : ActivityLifecycleCallbacks by delegate {
            override fun onActivityDestroyed(activity: Activity) {

            }
        }

    }


    companion object {

        private var insatnce: App? = null

        fun getInstance(): App {
            return insatnce!!
        }
    }
}
