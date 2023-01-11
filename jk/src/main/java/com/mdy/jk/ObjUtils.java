package com.mdy.jk;

/**
 * User: maodayu
 * Date: 2023/1/5
 * Time: 16:07
 * 生产者，消费者模型
 */
public class ObjUtils {

    private static Object obj = new Object();
    private static int FULL = 5;
    private static int count = 0;


    public static void main(String[] args) throws InterruptedException {

        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(new Producer());
            thread.start();
        }

        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(new Consumer());
            thread.start();
        }
    }


    static class Producer implements Runnable {

        @Override
        public void run() {

            synchronized (obj) {

                /**
                 * 之类必须使用while，因为当队列已满时，释放锁等待唤醒，被唤醒时有可能队列还是满的
                 * 所以需要while循环判断，消费者同理
                 */
                while (count == FULL) {
                    try {
                        Thread.sleep(1000);
                        System.out.println("生产者 thread -  " + Thread.currentThread().getName() + "队列满了");
                        obj.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                count++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("生产者 thread -  " + Thread.currentThread().getName() + "  生产一个产品 ");

                obj.notifyAll();
            }
        }
    }


    static class Consumer implements Runnable {

        @Override
        public void run() {

            synchronized (obj) {
                while (count == 0) {
                    try {
                        Thread.sleep(1000);
                        System.out.println("消费者 thread -  " + Thread.currentThread().getName() + "队列空了 ");
                        obj.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                count--;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("消费者 thread -  " + Thread.currentThread().getName() + "  消费了一个产品 ");

                obj.notifyAll();
            }
        }
    }
}
