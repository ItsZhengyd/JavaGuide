package juc;

import util.SmallTool;

import java.util.Enumeration;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;

/**
 * <a href="https://mp.weixin.qq.com/s/RNsHdEhijlkj9CfwK83bbQ">JUC 常用并发工具类一网打尽</a>
 * <p>CyclicBarrier,俗称栅栏锁,作用是让一组线程到达某个屏障,被阻塞,一直到组内的最后一个线程到达,然后屏障开放,接着,所有的线程继续运行</p>
 */
public class CyclicBarrierTest {

    /**
     * 存放子线程工作结果的安全容器
     */
    private static final ConcurrentHashMap<String, Long> resultMap = new ConcurrentHashMap<>();

    private static final CyclicBarrier cyclicBarrier = new CyclicBarrier(5,new CollectThread());

    /**
     * 结果打印线程
     * 用来演示CyclicBarrier的第二个参数,barrierAction
     */
    private static class CollectThread implements Runnable {

        @Override
        public void run() {
            SmallTool.printTimeAndThread("所有线程已到栅栏锁 do other business.....");
            SmallTool.printTimeAndThread("栅栏锁线程分别为： " + resultMap.values());
        }
    }

    /**
     * 工作子线程
     * 用于CyclicBarrier的一组线程
     */
    private static class SubThread implements Runnable {

        @Override
        public void run() {

            // 获取当前线程的ID
            long id = Thread.currentThread().getId();

            // 放入统计容器中
            resultMap.put(String.valueOf(id), id);

            Random random = new Random();

            try {
                if (random.nextBoolean()) {
                    SmallTool.sleepMillis(1000+random.nextInt(20));
                    SmallTool.printTimeAndThread("..... do something");
                }
                SmallTool.printTimeAndThread("await");
                cyclicBarrier.await();
                SmallTool.sleepMillis(1000+random.nextInt(20));
                SmallTool.printTimeAndThread(".....do its business");
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }

        }
    }

    public static void main(String[] args) {

        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(new SubThread());
            thread.start();
        }
    }

}
