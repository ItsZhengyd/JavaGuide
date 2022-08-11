public class JoinTest {

    public static void main(String[] args) {
        Thread previous = Thread.currentThread();
        for (int i = 0; i < 10; i++) {
            // 每个线程拥有前一个线程的引用，需要等待前一个线程终止，才能从等待中返回
            Thread thread = new Thread(new MyRunnable(previous), String.valueOf(i));
            thread.start();
            previous = thread;
        }
        SmallTool.sleepMillis(1000);
        SmallTool.printTimeAndThread("terminate.");
    }

    static class MyRunnable implements Runnable {
        private final Thread thread;

        public MyRunnable(Thread thread) {
            this.thread = thread;
        }

        @Override
        public void run() {
            try {
                thread.join();
            } catch (InterruptedException ignored) {
                SmallTool.printTimeAndThread("terminate.ignored");
            }
            SmallTool.printTimeAndThread("terminate.");
        }
    }

}
