package threadpool;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.*;
import com.sun.istack.internal.Nullable;
import org.junit.jupiter.api.Test;
import util.SmallTool;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class MoreExecutorsTest {

    ListeningExecutorService les = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));

    /**
     * 监听结果
     */
    @Test
    public void MoreExecutors_1() {

        ListenableFuture<String> future = les.submit(() -> {
            SmallTool.sleepMillis(2000);
            return "result";
        });

        // 监听方式 1
        future.addListener(() -> {
            try {
                SmallTool.printTimeAndThread("addListener" + future.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }, les);

        // 监听方式 2 (推荐)：通过工具类Futures来异步结果处理
        Futures.addCallback(future, new FutureCallback<String>() {
            //成功时处理
            @Override
            public void onSuccess(@Nullable String result) {
                SmallTool.printTimeAndThread("addCallback" + result);
            }

            //失败处理
            @Override
            public void onFailure(Throwable t) {
                SmallTool.printTimeAndThread(t.getMessage());
            }
        }, les);

        // 对Future结果再次处理，返回新的Future,但是会阻塞主线程，还是建议不要用该方法，而是去手动处理
//        ListenableFuture<String> newFuture = Futures.transform(future, input -> input + 1, les);
//        SmallTool.printTimeAndThread(newFuture.get());
        SmallTool.printTimeAndThread("main");
        SmallTool.sleepMillis(500);
    }

    /**
     * 批量执行任务&批量获取结果
     */
    @Test
    public void MoreExecutors_2() throws ExecutionException, InterruptedException {
        ListeningExecutorService les = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3));
        List<String> list = Lists.newArrayList("one", "two", "three");
        // 创建并执行任务
        List<ListenableFuture<String>> fList = list.stream().map(t -> les.submit(() -> {
            SmallTool.sleepMillis(200);
            if (t.equals("one")) {
//                throw new RuntimeException("ex");
            }
            return t;
        })).collect(Collectors.toList());

        // 【重点1：同时成功或失败】对future 处理，返回包含所有结果的Future，结果顺序与输入顺序是一致的，若其中有一个失败或取消，则整个Future为失败，抛出异常
        ListenableFuture<List<String>> resFuList = Futures.allAsList(fList);
        SmallTool.printTimeAndThread(resFuList.isDone() ? "完成" : "未完成");
        for (String result : resFuList.get()) {
            SmallTool.printTimeAndThread(result);
        }

        // 【重点2：以null代替失败】返回包含所有结果的Future，结果与输入顺序是一致的，若其中有一个失败或取消，则其future包含的值用null代替
        ListenableFuture<List<String>> succCombiner = Futures.successfulAsList(fList);
        SmallTool.printTimeAndThread(succCombiner.isDone() ? "完成" : "未完成");
        for (String result : succCombiner.get()) {
            SmallTool.printTimeAndThread(result);
        }
    }

}
