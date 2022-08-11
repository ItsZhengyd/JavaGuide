import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FutureTest {

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    @Test
    public void future() throws ExecutionException, InterruptedException {
        Future<String> submit = executor.submit(() -> {
            System.out.println(Thread.currentThread().getName());
            return "success";
        });
        System.out.println("submit.get() = " + submit.get());
    }

    /**
     * 参考 {@link CreateThreadTest#createThread_3()}
     */
    @Test
    public void futureTask() {

    }

}
