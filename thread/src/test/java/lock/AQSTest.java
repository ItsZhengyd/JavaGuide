package lock;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * <a href="https://mp.weixin.qq.com/s/uGUvHDcZ_bCAHIDX_NzFMQ">万字超强图解：AQS 以及 ReentrantLock 应用</a>
 * <p>队列同步器 {@link java.util.concurrent.locks.AbstractQueuedSynchronizer} 简称同步器或AQS
 * <p>一、AQS和锁什么关系？</p>
 * <ul>
 *     <li>
 *         锁是面向使用者的
 *         <p>它定义了使用者与锁交互的接口，隐藏了实现细节，我们就像范式那么使用就可以了</p>
 *     </li>
 *     <li>
 *         同步器面向锁的实现者
 *         <p>比如Doug Lea,或我们自定义的同步器，它简化了锁的实现方式，屏蔽了同步状态管理，线程排队，等待/唤醒等底层操作</p>
 *     </li>
 * </ul>
 *
 * <p>二、AQS可重写的方法</p>
 * <ul>
 *     <li>{@link AbstractQueuedSynchronizer#tryAcquire(int)} 独占式获取同步状态</li>
 *     <li>{@link AbstractQueuedSynchronizer#tryRelease(int)} 独占式释放同步状态</li>
 *     <li>{@link AbstractQueuedSynchronizer#tryAcquireShared(int)} 共享式获取同步状态，返回值>=0表示获取成功，反之获取失败</li>
 *     <li>{@link AbstractQueuedSynchronizer#tryReleaseShared(int)} 共享式释放同步状态</li>
 *     <li>{@link AbstractQueuedSynchronizer#isHeldExclusively()} 当前同步器是否在独占模式下被线程使用，一般该方法表示是否被当前线程独占</li>
 * </ul>
 * <p>{@link java.util.concurrent.locks.ReentrantLock}
 * <p>{@link java.util.concurrent.locks.ReentrantReadWriteLock}、
 * <p>{@link java.util.concurrent.Semaphore}
 * <p>{@link java.util.concurrent.CountDownLatch}
 * <p>这几个类其实仅仅是在实现以上几个方法上略有差别，其他的实现都是通过同步器的模版方法来实现的
 * <p>1. 需要重写的方法没有用 abstract 来修饰是为了避免强制重写不相干方法
 * <pre>
 *     {@code     protected boolean tryAcquire(int arg) {
 *         throw new UnsupportedOperationException();
 *     }}
 * </pre>
 * <p>2. 同步状态就是{@link LockTest}提到的有 volatile 修饰的 state，所以我们在重写上面几个方法时，还要通过同步器提供的下面三个方法（AQS 提供的）来获取或修改同步状态：
 * <ul>
 *     <li>{@code AbstractQueuedSynchronizer#getState()} 获取当前同步状态</li>
 *     <li>{@code AbstractQueuedSynchronizer#setState(int)} 设置当前同步状态</li>
 *     <li>{@code AbstractQueuedSynchronizer#compareAndSetState(int, int)} 使用CAS设置当前同步状态，该方法会保证同步状态设置的原子性</li>
 * </ul>
 * 独占式和共享式操作 state 变量的区别：
 * <ul>
 *     <li>独占式 state 0<——>1</li>
 *     <li>共享式 state 0<——>N</li>
 * </ul>
 *
 * <p>三、AQS提供的模板方法</p>
 * <p>上面我们将同步器的实现方法分为独占式和共享式两类，模版方法其实除了提供以上两类模版方法之外，只是多了响应中断和超时限制 的模版方法供 Lock 使用
 * <ul>
 *     <li>{@link AbstractQueuedSynchronizer#acquire(int)} <p>独占式获取同步状态，获取同步状态成功则返回，失败则进入同步等待队列</li>
 *     <li>{@link AbstractQueuedSynchronizer#acquireInterruptibly(int)} <p>与acquire一样，只不过响应终端，当前线程获取同步状态失败进入等待队列，如果被中断，该方法会抛出{@link InterruptedException}并返回</li>
 *     <li>{@link AbstractQueuedSynchronizer#tryAcquireNanos(int, long)} <p>在acquireInterruptibly的基础上增加了超时限制，超时未获取到同步状态返回true，否则返回false</li>
 *     <li>{@link AbstractQueuedSynchronizer#release(int)}</li> <p>独占式释放同步状态
 *     <li>----------</li>
 *     <li>{@link AbstractQueuedSynchronizer#acquireShared(int)} <p>共享式获取同步状态，与acquire的区别是同一时刻可以有多个线程获取到同步状态</li>
 *     <li>{@link AbstractQueuedSynchronizer#acquireSharedInterruptibly(int)} <p>与acquireShared相同，响应中断</li>
 *     <li>{@link AbstractQueuedSynchronizer#tryAcquireSharedNanos(int, long)} <p>与acquireSharedInterruptibly相同，有超时限制</li>
 *     <li>{@link AbstractQueuedSynchronizer#releaseShared(int)} <p>共享式释放同步状态</li>
 * </ul>
 */
public class AQSTest {

    @Test
    public void AQS() {
        Lock lock = new MyMutex();
        lock.lock();
    }

    /**
     * 从{@link MyMutex}中，你应该理解了lock.tryLock() 非阻塞式获取锁就是调用自定义同步器重写的 tryAcquire() 方法，
     * 通过 CAS 设置state 状态，不管成功与否都会马上返回；那么 lock.lock() 这种阻塞式的锁是如何实现的呢？
     * <p>有阻塞就需要排队，实现排队必然需要队列</p>
     * <p>CLH：Craig、Landin and Hagersten 队列，是一个单向链表，AQS中的队列是CLH变体的虚拟双向队列（FIFO）</p>
     * <p>队列中每个排队的个体就是一个 {@link java.util.concurrent.locks.AbstractQueuedSynchronizer.Node}</p>
     */
    @Test
    public void CLH(){

    }

    /**
     * AQS 内部维护了一个同步队列，用于管理同步状态。
     * <ul>
     *     <li>当线程获取同步状态失败时，就会将当前线程以及等待状态等信息构造成一个 Node 节点，将其加入到同步队列中尾部，阻塞该线程</li>
     *     <li>当同步状态被释放时，会唤醒同步队列中“首节点”的线程获取同步状态</li>
     * </ul>
     * {@link java.util.concurrent.locks.AbstractQueuedSynchronizer.Node}属性说明
     * <ul>
     *     <li>{@link java.util.concurrent.locks.AbstractQueuedSynchronizer.Node#prev} 前驱节点</li>
     *     <li>{@link java.util.concurrent.locks.AbstractQueuedSynchronizer.Node#next} 后继节点</li>
     *     <li>{@link java.util.concurrent.locks.AbstractQueuedSynchronizer.Node#thread} 获取同步状态的线程</li>
     *     <li>{@link java.util.concurrent.locks.AbstractQueuedSynchronizer.Node#waitStatus} 等待状态</li>
     *     <li>{@link java.util.concurrent.locks.AbstractQueuedSynchronizer.Node#nextWaiter} 指向下一个处在CONDITION状态的节点</li>
     * </ul>
     * {@link java.util.concurrent.locks.AbstractQueuedSynchronizer.Node#waitStatus} 等待状态枚举
     * <ul>
     *     <li>{@link java.util.concurrent.locks.AbstractQueuedSynchronizer.Node#CANCELLED} 1 由于超时或中断，线程获取锁的请求取消了，节点变成该状态不会再有变化</li>
     *     <li>{@link java.util.concurrent.locks.AbstractQueuedSynchronizer.Node#SIGNAL} -1 表示线程已经准备好了，等待资源释放</li>
     *     <li>{@link java.util.concurrent.locks.AbstractQueuedSynchronizer.Node#CONDITION} -2 表示线程在条件队列中，等待被唤醒</li>
     *     <li>{@link java.util.concurrent.locks.AbstractQueuedSynchronizer.Node#PROPAGATE} -3 当线程处于SHARED情况下，该字段才会使用</li>
     *     <li>0 指向下一个处在CONDITION状态的节点</li>
     * </ul>
     */
    @Test
    public void Node(){

    }

    /**
     * <p>一、独占式获取同步状态</p>
     * <p>{@link MyMutex#lock()} 阻塞式的获取锁，调用同步器模版方法，获取同步状态
     * <p>{@link AbstractQueuedSynchronizer#acquire(int)}
     * <pre>
     *     {@code public final void acquire(int arg) {
     *   // 调用自定义同步器重写的 tryAcquire 方法
     *  if (!tryAcquire(arg) &&
     *   acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
     *   selfInterrupt();
     * }}
     * </pre>
     * <p>首先，也会尝试非阻塞的获取同步状态，如果获取失败（tryAcquire返回false），则会调用 addWaiter 方法构造 Node 节点
     * （Node.EXCLUSIVE 独占式）并安全的（CAS）加入到同步队列【尾部】</p>
     */
    @Test
    public void lock(){

    }

}

/**
 * 自定义互斥锁,以下类都是按照这个结构实现
 * <p>{@link java.util.concurrent.locks.ReentrantLock}
 * <p>{@link java.util.concurrent.locks.ReentrantReadWriteLock}、
 * <p>{@link java.util.concurrent.Semaphore}
 * <p>{@link java.util.concurrent.CountDownLatch}
 */
class MyMutex implements Lock {

    // 静态内部类-自定义同步器
    private static class MySync extends AbstractQueuedSynchronizer {
        @Override
        protected boolean tryAcquire(int arg) {
            // 调用AQS提供的方法，通过CAS保证原子性
            if (compareAndSetState(0, arg)) {
                // 我们实现的是互斥锁，所以标记获取到同步状态（更新state成功）的线程，
                // 主要为了判断是否可重入（一会儿会说明）
                setExclusiveOwnerThread(Thread.currentThread());
                //获取同步状态成功，返回 true
                return true;
            }
            // 获取同步状态失败，返回 false
            return false;
        }

        @Override
        protected boolean tryRelease(int arg) {
            // 未拥有锁却让释放，会抛出IMSE
            if (getState() == 0) {
                throw new IllegalMonitorStateException();
            }
            // 可以释放，清空排它线程标记
            setExclusiveOwnerThread(null);
            // 设置同步状态为0，表示释放锁
            setState(0);
            return true;
        }

        // 是否独占式持有
        @Override
        protected boolean isHeldExclusively() {
            return getState() == 1;
        }

        // 后续会用到，主要用于等待/通知机制，每个condition都有一个与之对应的条件等待队列，在锁模型中说明过
        Condition newCondition() {
            return new ConditionObject();
        }
    }

    // 聚合自定义同步器
    private final MySync sync = new MySync();


    @Override
    public void lock() {
        // 阻塞式的获取锁，调用同步器模版方法独占式，获取同步状态
        sync.acquire(1);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        // 调用同步器模版方法可中断式获取同步状态
        sync.acquireInterruptibly(1);
    }

    @Override
    public boolean tryLock() {
        // 调用自己重写的方法，非阻塞式的获取同步状态
        return sync.tryAcquire(1);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        // 调用同步器模版方法，可响应中断和超时时间限制
        return sync.tryAcquireNanos(1, unit.toNanos(time));
    }

    @Override
    public void unlock() {
        // 释放锁
        sync.release(1);
    }

    @Override
    public Condition newCondition() {
        // 使用自定义的条件
        return sync.newCondition();
    }
}