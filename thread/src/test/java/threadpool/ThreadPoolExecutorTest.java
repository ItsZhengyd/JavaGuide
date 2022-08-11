package threadpool;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import util.SmallTool;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 示例 {@link ThreadPoolExecutor} 所有参数的用法
 *
 * <p>ThreadPoolExecutor 参数</p>
 * <ol>
 *     <li>corePoolSize 核心线程数</li>
 *     <li>maximumPoolSize 最大线程数</li>
 *     <li>keepAliveTime 非核心线程存活时间(当 allowCoreThreadTimeout 设置为 true 时，核心线程也会超时回收)</li>
 *     <li>unit 存活时间单位</li>
 *     <li>workQueue 工作线程</li>
 *     <li>threadFactory 线程工厂(可选)</li>
 *     <li>handler 拒绝策略(可选)</li>
 * </ol>
 * 核心线程可回收的意义在于刚开始需要很多线程，后来就很少需要了
 * 场景：系统刚启动有很多缓存需要初始化，以后就不会再初始化了
 *
 * <p>jdk默认实现的阻塞队列</p>
 * <ol>
 *     <li>{@link ArrayBlockingQueue}</li>
 *     <li>{@link SynchronousQueue}</li>
 *     <li>{@link LinkedBlockingDeque}</li>
 *     <li>{@link LinkedTransferQueue}</li>
 *     <li>{@link LinkedBlockingQueue}</li>
 *     <li>{@link PriorityBlockingQueue}</li>
 * </ol>
 *
 * <p>ThreadPoolExecutor 默认实现的拒绝策略</p>
 * <ol>
 *     <li>{@link java.util.concurrent.ThreadPoolExecutor.AbortPolicy} 丢弃任务并抛出 RejectedExecutionException 异常。</li>
 *     <li>{@link java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy} 由调用线程处理该任务。</li>
 *     <li>{@link java.util.concurrent.ThreadPoolExecutor.DiscardPolicy} 丢弃任务，但是不抛出异常。可以配合这种模式进行自定义的处理方式。</li>
 *     <li>{@link java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy} 丢弃队列最早的未处理任务，然后重新尝试执行任务。</li>
 * </ol>
 *
 */
public class ThreadPoolExecutorTest {

    public static ThreadPoolExecutor threadPoolExecutor;

    @BeforeAll
    public static void init() {
        int corePoolSize = 5;
        int maximumPoolSize = 10;
        long keepAliveTime = 1000;
        TimeUnit unit = TimeUnit.MILLISECONDS;
        BlockingQueue<Runnable> workQueue = new PriorityBlockingQueue<>(1000);
        ThreadFactory threadFactory = new DefaultThreadFactory();
        RejectedExecutionHandler handler = new MyRejectedExecutionHandler();
        threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        threadPoolExecutor.allowCoreThreadTimeOut(true);
    }

    /**
     * 线程池队列使用 PriorityBlockingQueue 时，threadPoolExecutor.submit会报错
     */
    @Test
    public void test() {
        threadPoolExecutor.execute(new MyRunnable());
    }

    static class MyRunnable implements Runnable, Comparable<String> {

        @Override
        public int compareTo(String o) {
            return 0;
        }

        @Override
        public void run() {
            SmallTool.printTimeAndThread("task");
        }
    }

    /**
     * 来自 {@link Executors}
     */
    static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

    static class MyRejectedExecutionHandler implements RejectedExecutionHandler {

        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                r.run();
            }
        }
    }
}
