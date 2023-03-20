package com.mdy.jk;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: maodayu
 * Date: 2023/1/5
 * Time: 19:02
 */
public class VolatileUtils {

    /**
     * 可见性：当volatile修饰的变量被修改时，JMM会将线程本地的变量强制刷新到主内存
     * 有序性：通过插入内存屏障来禁止JMM的指令重排
     */
   // public static volatile int vlEvent = 0;

    public static AtomicInteger vlEvent = new AtomicInteger();


    public static void increase() {
        vlEvent.incrementAndGet();
    }

    public static void main(String[] args) {

        for (int i = 0; i < 10; i++) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < 10000; j++) {
                        increase();
                    }
                }
            }).start();
        }


        while (Thread.activeCount() > 1){
            Thread.yield();
        }

        System.out.println(" vlEvent : " +vlEvent);
    }
}
