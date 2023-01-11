package com.mdy.jk

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread

/**
 * User: maodayu
 * Date: 2023/1/5
 * Time: 14:43
 * 锁的实现和基本原理
 */
object LockUtils {

    private val lock = ReentrantLock()
    private val pCondition = lock.newCondition()
    private val cCondition = lock.newCondition()


    @JvmStatic
    fun main(args: Array<String>) {

        for (index in 0 until 20) {
            thread {
                writeMsg("-----" + index)
            }
        }
    }


    private fun writeMsg(msg: String) {

        lock.lock()

        //尝试获取锁，获取失败不会进入队列
        //lock.tryLock()
        try {
            Thread.sleep(200)
            println(msg + "    thread:" + Thread.currentThread().name)
        } finally {
            lock.unlock()
        }
    }
}