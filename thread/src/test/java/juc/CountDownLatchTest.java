package juc;

import util.SmallTool;

import java.util.concurrent.CountDownLatch;

/**
 * <a href="https://mp.weixin.qq.com/s/RNsHdEhijlkj9CfwK83bbQ">JUC 常用并发工具类一网打尽</a>
 * <p>CountDownLatch,俗称闭锁,作用是类似加强版的Join,是让一组线程等待其他的线程完成工作以后才执行
 * 就比如在启动框架服务的时候,我们主线程需要在环境线程初始化完成之后才能启动,这时候我们就可以实现使用CountDownLatch来完成
 *
 * <p>{@link CountDownLatch#await()} 线程会阻塞线程，直到{@link CountDownLatch#countDown()}全部完成</p>
 */
public class CountDownLatchTest {

    /**
     * 设置为6个扣除点
     */
    static CountDownLatch countDownLatch = new CountDownLatch(6);

    /**
     * 初始化线程
     */
    private static class InitThread implements Runnable {

        @Override
        public void run() {

            SmallTool.printTimeAndThread("ready init work .....");

            // 执行扣减 扣减不代表结束
            countDownLatch.countDown();

            for (int i = 0; i < 2; i++) {
                SmallTool.printTimeAndThread(".....continue do its work");
            }

        }
    }

    /**
     * 业务线程
     */
    private static class BusiThread implements Runnable {

        @Override
        public void run() {

            // 业务线程需要在等初始化完毕后才能执行
            try {
                countDownLatch.await();
                for (int i = 0; i < 3; i++) {
                    SmallTool.printTimeAndThread("do business-----");

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

        // 创建单独的初始化线程
        new Thread(() -> {
            SmallTool.sleepMillis(1);
            SmallTool.printTimeAndThread("ready init work step 1st.....");
            // 扣减一次
            countDownLatch.countDown();
            SmallTool.printTimeAndThread("begin stop 2nd.....");
            SmallTool.sleepMillis(1);
            SmallTool.printTimeAndThread("ready init work step 2st.....");
            // 扣减一次
            countDownLatch.countDown();

        }).start();
        // 启动业务线程
        new Thread(new BusiThread()).start();
        // 启动初始化线程
        for (int i = 0; i <= 3; i++) {
            new Thread(new InitThread()).start();
        }
        // 主线程进入等待
        try {
            countDownLatch.await();
            SmallTool.printTimeAndThread("Main do ites work.....");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
