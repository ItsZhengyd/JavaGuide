import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * 创建线程
 * <ol>
 *     <li>继承 {@link Thread} 类，重写 {@link Thread#run()} 方法</li>
 *     <li>实现 {@link Runnable} 接口，重写 {@link Runnable#run()} 方法</li>
 *     <li>实现 {@link Callable} 接口，重写 {@link Callable#call()} 方法</li>
 * </ol>
 *
 */
public class CreateThreadTest {

    /**
     * 继承 {@link Thread} 类，重写 {@link Thread#run()} 方法
     */
    @Test
    public void createThread_1(){
        new MyThread().start();
    }

    /**
     * 实现 {@link Runnable} 接口，重写 {@link Runnable#run()} 方法
     */
    @Test
    public void createThread_2(){
        new Thread(new MyRunnable()).start();
    }

    /**
     * 实现 {@link Callable} 接口，重写 {@link Callable#call()} 方法
     * @throws ExecutionException 执行异常
     * @throws InterruptedException 中断异常
     */
    @Test
    public void createThread_3() throws ExecutionException, InterruptedException {
        FutureTask<String> task =  new FutureTask<>(new MyCallable());
        new Thread(task).start();
        System.out.println("task = " + task.get());
    }

    static class MyThread extends Thread {
        @Override
        public void run() {
            System.out.println(this.getName());
        }
    }

    static class MyRunnable implements Runnable{
        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName());
        }
    }

    static class MyCallable implements Callable<String> {
        @Override
        public String call() {
            return "success";
        }
    }
}
