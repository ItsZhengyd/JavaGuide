package threadpool;

import org.junit.jupiter.api.Test;
import util.SmallTool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class ExecutorServiceTest {

    public ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * {@link ExecutorService#execute(Runnable)}
     * 该方法继承自 interface Executor ，异步，无返回结果
     */
    @Test
    public void executeRunnable() {
        executorService.execute(() -> SmallTool.printTimeAndThread("executorService task"));
        SmallTool.printTimeAndThread("main");
    }

    /**
     * {@link ExecutorService#submit(Runnable)}
     * 异步方法，但 future.get() 会阻塞主线程，仅返回是否完成，不返回结果
     */
    @Test
    public void submitRunnable() throws ExecutionException, InterruptedException {
        Future<?> future = executorService.submit(() -> {
            SmallTool.sleepMillis(100);
            SmallTool.printTimeAndThread("executorService task");
        });
        SmallTool.printTimeAndThread(future.isDone() ? "任务完成" : "任务未完成");
        if (null == future.get()) {
            SmallTool.printTimeAndThread("任务完成");
        }
    }

    /**
     * {@link ExecutorService#submit(Runnable, Object)}
     * 异步方法，但 future.get() 会阻塞主线程，仅返回是否完成（Object未自定义的完成标识），不返回结果
     */
    @Test
    public void submitRunnableWithCustomResult() throws ExecutionException, InterruptedException {
        Future<Boolean> future = executorService.submit(() -> {
            SmallTool.sleepMillis(100);
            SmallTool.printTimeAndThread("executorService task");
        }, Boolean.TRUE);
        SmallTool.printTimeAndThread(future.isDone() ? "任务完成" : "任务未完成");
        if (future.get()) {
            SmallTool.printTimeAndThread("任务完成");
        }
    }

    /**
     * {@link ExecutorService#submit(Callable)}
     * 异步方法，但 future.get() 会阻塞主线程，返回结果 T，可以抛出异常
     */
    @Test
    public void submitCallable() throws ExecutionException, InterruptedException {
        Future<String> future = executorService.submit(() -> {
            SmallTool.sleepMillis(100);
            SmallTool.printTimeAndThread("executorService task");
            return "success";
        });
        SmallTool.printTimeAndThread(future.isDone() ? "任务完成" : "任务未完成");
        if (future.get().equals("success")) {
            SmallTool.printTimeAndThread("任务完成");
        }
    }

    /**
     * {@link ExecutorService#invokeAll(Collection)}
     * 异步方法，可以批量执行任务
     */
    @Test
    public void invokeAll() throws InterruptedException, ExecutionException {
        // 定义任务集合
        List<Callable<String>> taskList = new ArrayList<>();
        taskList.add(() -> {
            SmallTool.printTimeAndThread("a");
            return "a";
        });
        taskList.add(() -> {
            SmallTool.printTimeAndThread("b");
            return "b";
        });
        taskList.add(() -> {
            SmallTool.sleepMillis(100);
            SmallTool.printTimeAndThread("c");
            return "c";
        });

        // 执行任务集合
        List<Future<String>> futures = executorService.invokeAll(taskList);

        // 处理结果
        for (Future<String> future : futures) {
            SmallTool.printTimeAndThread(future.get());
        }
    }

    /**
     * 批量执行任务，但是只要其中一个完成即返回结果，其它任务将被取消
     * 场景：海量数据文件系统中查找唯一资源，可以多线程多目录一起查找
     */
    @Test
    public void invokeAny() throws ExecutionException, InterruptedException {
        List<Callable<String>> taskList = new ArrayList<>();
        taskList.add(() -> {
            SmallTool.printTimeAndThread("a");
            return "a";
        });
        taskList.add(() -> {
            SmallTool.printTimeAndThread("b");
            return "b";
        });
        taskList.add(() -> {
            SmallTool.sleepMillis(100);
            SmallTool.printTimeAndThread("c");
            return "c";
        });

        // 执行任务集合
        String result = executorService.invokeAny(taskList);
        SmallTool.printTimeAndThread(result);
    }


}
