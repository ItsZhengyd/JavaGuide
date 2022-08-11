import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <a href="https://www.bilibili.com/video/BV1wZ4y1A7PK">参考视频</a>
 * <a href="https://gitee.com/phui/share-concurrent/">视频代码</a>
 */
public class CompletableFutureTest {

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    /**
     * {@link CompletableFuture#supplyAsync(Supplier)}
     * 作用类似 {@link FutureTest#future()}, 但是不会显式抛出异常
     */
    @Test
    public void supplyAsync() {
        CompletableFuture<String> result = CompletableFuture.supplyAsync(() -> {
            SmallTool.printTimeAndThread("supplyAsync");
            return "supplyAsync";
        }, executor);
        SmallTool.printTimeAndThread("result:" + result.join());
    }

    /**
     * {@link CompletableFuture#thenCompose(Function)}
     * 多线程顺序执行 thenCompose
     * <ol>
     *      <li>小白进入了餐厅 main</li>
     *      <li>小白点了番茄炒蛋+米饭 main</li>
     *      <li>厨师炒饭 pool-1-thread-1</li>
     *      <li>小白打王者 main</li>
     *      <li>服务员打饭 pool-1-thread-2</li>
     *      <li>番茄炒蛋 + 米饭,小白开吃 main</li>
     * </ol>
     */
    @Test
    public void thenCompose() {
        SmallTool.printTimeAndThread("小白进入了餐厅");
        SmallTool.printTimeAndThread("小白点了番茄炒蛋+米饭");
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            SmallTool.printTimeAndThread("厨师炒饭");
            return "番茄炒蛋";
        }, executor).thenCompose(dish -> {
            SmallTool.printTimeAndThread("服务员A准备打饭，但是被领导叫走，打饭交接给服务员B");
            return CompletableFuture.supplyAsync(() -> {
                SmallTool.printTimeAndThread("服务员B打饭");
                return dish + " + 米饭";
            }, executor);
        });
        SmallTool.printTimeAndThread("小白打王者");
        SmallTool.printTimeAndThread(completableFuture.join() + "小白开吃");
    }

    /**
     * 和{@link CompletableFuture#thenCompose(Function)}的区别在于对 “服务员A” 的处理
     * 如果没有 “服务员A” 这一行漏极，则 thenComposeAsync 和 thenCompose 无差别
     */
    @Test
    public void thenComposeAsync() {
        SmallTool.printTimeAndThread("小白进入了餐厅");
        SmallTool.printTimeAndThread("小白点了番茄炒蛋+米饭");
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            SmallTool.printTimeAndThread("厨师炒饭");
            return "番茄炒蛋";
        }, executor).thenComposeAsync(dish -> {
            SmallTool.printTimeAndThread("服务员A准备打饭，但是被领导叫走，打饭交接给服务员B");
            return CompletableFuture.supplyAsync(() -> {
                SmallTool.printTimeAndThread("服务员B打饭");
                return dish + " + 米饭";
            }, executor);
        });
        SmallTool.printTimeAndThread("小白打王者");
        SmallTool.printTimeAndThread(completableFuture.join() + "小白开吃");
    }

    /**
     * {@link CompletableFuture#supplyAsync(Supplier)}
     * {@link CompletableFuture#thenCombine(CompletionStage, BiFunction)}
     * 多线程异步执行最后汇合结果 thenCombine
     */
    @Test
    public void thenCombine() {
        SmallTool.printTimeAndThread("小白进入了餐厅");
        SmallTool.printTimeAndThread("小白点了番茄炒蛋+米饭");
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            SmallTool.printTimeAndThread("厨师炒饭");
            return "番茄炒蛋";
        }, executor).thenCombine(CompletableFuture.supplyAsync(() -> {
            SmallTool.printTimeAndThread("服务员打饭");
            return "米饭";
        }, executor), (dish, rice) -> dish + rice);
        SmallTool.printTimeAndThread("小白打王者");
        SmallTool.printTimeAndThread(completableFuture.join() + ",小白开吃");
    }

    /**
     * {@link CompletableFuture#supplyAsync(Supplier)}
     * {@link CompletableFuture#thenApply(Function)}
     * 多线程顺序执行 thenApply
     * 用途类似 thenCompose，但是执行线程有些许差异：
     * thenApply后的任务无法指定线程，这部分任务可能使用supplyAsync线程执行，也可能使用main线程执行，
     * 所以根据”服务员打饭“使用线程不同大概有以下两种(总的顺序种类不止两种)执行结果
     * <ol>
     *     <li>小白进入了餐厅 main</li>
     *     <li>小白点了番茄炒蛋+米饭 main</li>
     *     <li>厨师炒饭 pool-1-thread-1</li>
     *     <li>服务员打饭 main</li>
     *     <li>小白打王者 main</li>
     *     <li>番茄炒蛋 + 米饭,小白开吃 main</li>
     * </ol>
     * <ol>
     *     <li>小白进入了餐厅 main</li>
     *     <li>小白点了番茄炒蛋+米饭 main</li>
     *     <li>小白打王者 main</li>
     *     <li>厨师炒饭 pool-1-thread-1</li>
     *     <li>服务员打饭 pool-1-thread-1</li>
     *     <li>番茄炒蛋 + 米饭,小白开吃 main</li>
     * </ol>
     */
    @Test
    public void thenApply() {
        SmallTool.printTimeAndThread("小白进入了餐厅");
        SmallTool.printTimeAndThread("小白点了番茄炒蛋+米饭");
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            SmallTool.printTimeAndThread("厨师炒饭");
            return "番茄炒蛋";
        }, executor).thenApply(dish -> {
            SmallTool.printTimeAndThread("服务员打饭");
            return dish + " + 米饭";
        });
        SmallTool.printTimeAndThread("小白打王者");
        SmallTool.printTimeAndThread(completableFuture.join() + ",小白开吃");
    }

    /**
     * {@link CompletableFuture#thenApplyAsync(Function)}
     * {@link CompletableFuture#thenApplyAsync(Function, Executor)}
     * 作用几乎完全同 {@link CompletableFuture#thenCompose(Function)}
     */
    @Test
    public void thenApplyAsync() {
        SmallTool.printTimeAndThread("小白进入了餐厅");
        SmallTool.printTimeAndThread("小白点了番茄炒蛋+米饭");
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            SmallTool.sleepMillis(200);
            SmallTool.printTimeAndThread("厨师炒饭");
            return "番茄炒蛋";
        }, executor).thenApplyAsync(dish -> {
            SmallTool.printTimeAndThread("服务员打饭");
            return dish + " + 米饭";
        }, executor);
        SmallTool.printTimeAndThread("小白打王者");
        SmallTool.printTimeAndThread(completableFuture.join() + ",小白开吃");
    }

    @Test
    public void applyToEither_exceptionally() {
        SmallTool.printTimeAndThread("张三等待 700路 或者 800路 公交到来");
        CompletableFuture<String> bus = CompletableFuture.supplyAsync(() -> {
            SmallTool.printTimeAndThread("700路公交正在赶来");
            SmallTool.sleepMillis(100);
            return "700路到了";
        }).applyToEither(CompletableFuture.supplyAsync(() -> {
            SmallTool.printTimeAndThread("800路公交正在赶来");
            SmallTool.sleepMillis(200);
            return "800路到了";
        }), firstComeBus -> {
            switch (firstComeBus.substring(0, 3)) {
                case "700":
                    SmallTool.printTimeAndThread(firstComeBus + ",小白坐上了700路公交车");
                    throw new RuntimeException("700路公交车故障");
                case "800":
                    SmallTool.printTimeAndThread(firstComeBus + ",小白坐上了800路公交车");
            }
            return firstComeBus;
        }).exceptionally(e -> {
            SmallTool.printTimeAndThread(e.getMessage());
            SmallTool.printTimeAndThread("小白叫出租车");
            return "出租车 叫到了";
        });
        SmallTool.printTimeAndThread(bus.join() + "，小白坐车回家");
    }

    /**
     * {@link CompletableFuture#allOf(CompletableFuture[])}
     * 多线程异步执行
     */
    @Test
    public void allOf() {
        Map<String, String> result = new HashMap<>();
        CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> result.put("key1", "value1"), executor),
                CompletableFuture.runAsync(() -> result.put("key2", "value2"), executor),
                CompletableFuture.runAsync(() -> result.put("key3", "value3"), executor)
        ).join();
        System.out.println("result = " + result);
    }

    static class SmallTool {
        public static void sleepMillis(int millis) {
            try {
                TimeUnit.MILLISECONDS.sleep(millis);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public static void printTimeAndThread(String tag) {
            String result = new StringJoiner("\t|\t")
                    .add(String.valueOf(System.currentTimeMillis()))
                    .add(String.valueOf(Thread.currentThread().getId()))
                    .add(Thread.currentThread().getName())
                    .add(tag)
                    .toString();
            System.out.println(result);
        }
    }

}
