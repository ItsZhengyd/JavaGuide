package threadpool;

import org.junit.jupiter.api.Test;
import util.SmallTool;

import java.util.concurrent.*;


/**
 * {@link Executors#newScheduledThreadPool(int)}
 * ScheduledThreadPoolExecutor 非核心线程 无限制,有耗尽资源的风险，不建议用
 */
public class ScheduledThreadPoolExecutorTest {

    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);
    MyRunnable myRunnable = new MyRunnable();

    @Test
    public void scheduleRunnable() {
        ScheduledFuture<?> schedule = scheduledExecutorService.schedule(myRunnable, 100, TimeUnit.MILLISECONDS);
        SmallTool.printTimeAndThread(schedule.isDone() ? "完成" : "未完成");
    }

    @Test
    public void scheduleCallable() {
        ScheduledFuture<Boolean> schedule = scheduledExecutorService.schedule(() -> Boolean.TRUE, 100, TimeUnit.MILLISECONDS);
        SmallTool.printTimeAndThread(schedule.isDone() ? "完成" : "未完成");
    }

    /**
     * initialDelay 初次延迟
     * period 周期：无视任务执行时间
     */
    @Test
    public void scheduleAtFixedRate() throws ExecutionException, InterruptedException {
        ScheduledFuture<?> schedule = scheduledExecutorService.scheduleAtFixedRate(myRunnable, 200, 500, TimeUnit.MILLISECONDS);
        schedule.get();
    }

    /**
     * initialDelay 初次延迟
     * delay 间隔：第一次任务执行完成到第二次任务执行开始的间隔
     */
    @Test
    public void scheduleWithFixedDelay() throws ExecutionException, InterruptedException {
        ScheduledFuture<?> schedule = scheduledExecutorService.scheduleWithFixedDelay(myRunnable, 200, 500, TimeUnit.MILLISECONDS);
        schedule.get();
    }

    static class MyRunnable implements Runnable {

        @Override
        public void run() {
            SmallTool.sleepMillis(200);
            SmallTool.printTimeAndThread("thread");
        }
    }
}
