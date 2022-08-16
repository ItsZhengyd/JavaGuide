package lock;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <a href="https://mp.weixin.qq.com/s/uGUvHDcZ_bCAHIDX_NzFMQ">万字超强图解：AQS 以及 ReentrantLock 应用</a>
 * <p>一、显式锁 Lock 相较 synchronized 的优势</p>
 * <ol>
 *     <li>
 *         <p>能响应中断</p>
 *         <p>如果不能自己释放，那可以响应中断也是很好的。Java多线程中断机制 专门描述了中断过程，目的是通过中断信号来跳出某种状态，比如阻塞</p>
 *         <p>{@link Lock#lockInterruptibly()}</p>
 *     </li>
 *     <li>
 *         <p>非阻塞式的获取锁</p>
 *         <p>尝试获取，获取不到不会阻塞，直接返回</p>
 *         <p>{@link Lock#tryLock()}</p>
 *     </li>
 *     <li>
 *         <p>支持超时</p>
 *         <p>给定一个时间限制，如果一段时间内没获取到，不是进入阻塞状态，同样直接返回</p>
 *         <p>{@link Lock#tryLock(long, TimeUnit)}</p>
 *     </li>
 * </ol>
 *
 * <p>二、Lock 是怎样起到锁的作用呢？</p>
 * <p>如果你熟悉 synchronized，你知道程序编译成 CPU 指令后，在临界区会有 moniterenter 和 moniterexit 指令的出现，可以理解成进出临界区的标识</p>
 * <p>从范式上来看：
 * <ul>
 *     <li>{@link Lock#lock()} 获取锁，“等同于” synchronized 的 moniterenter指令</li>
 *     <li>{@link Lock#unlock()} 释放锁，“等同于” synchronized 的 moniterexit 指令</li>
 * </ul>
 * <p>那 Lock 是怎么做到的呢？
 * <p>Lock是一个接口，接口定义行为，我们以 {@link ReentrantLock} 为例分析
 * <p>在 ReentrantLock 内部维护了一个 volatile 修饰的变量 state，通过 CAS 来进行读写（最底层还是交给硬件来保证原子性和可见性），
 * 如果CAS更改成功，即获取到锁，线程进入到 try 代码块继续执行；如果没有更改成功，线程会被【挂起】，不会向下执行
 * <p>Lock 接口的实现类基本都是通过【聚合】了一个【队列同步器】的子类{@link AQSTest}完成线程访问控制的
 */
public class LockTest {

    /**
     * <p>Lock使用范式</p>
     * <ol>
     *     <li>标准1—finally 中释放锁</li>
     *     <li>
     *         <p>标准2—在 try{} 外面获取锁</p>
     *         <p>如果没有获取到锁就抛出异常，最终释放锁肯定是有问题的，因为还未曾拥有锁谈何释放锁呢</p>
     *         <p>如果在获取锁时抛出了异常，也就是当前线程并未获取到锁，但执行到 finally 代码时，如果恰巧别的线程获取到了锁，则会被释放掉（无故释放）</p>
     *     </li>
     * </ol>
     * <pre>{@code
     * Lock lock = new ReentrantLock();
     * lock.lock();
     * try{
     *  ...
     * }finally{
     *  lock.unlock();
     * }
     * }<pre>
     */
    @Test
    public void test() {

        Lock lock = new ReentrantLock();
        try {
            System.out.println("do something...");
        } finally {
            lock.unlock();
        }
    }

}
