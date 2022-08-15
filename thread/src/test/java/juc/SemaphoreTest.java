package juc;

import util.SmallTool;

import java.sql.*;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

/**
 * 参考：<a href="https://mp.weixin.qq.com/s/RNsHdEhijlkj9CfwK83bbQ">JUC 常用并发工具类一网打尽</a>
 * <p>Semaphore,俗称信号量,作用于控制同时访问某个特定资源的线程数量,用在流量控制</p>
 * <ul>
 *     <li>{@link Semaphore#acquire()} 减少许可证</li>
 *     <li>{@link Semaphore#release()} 增加许可证</li>
 *     <li>{@link Semaphore#getQueueLength()} 当前队列长度</li>
 *     <li>{@link Semaphore#availablePermits()} 可使用的许可证数量</li>
 * </ul>
 */
public class SemaphoreTest {

    public static void main(String[] args) {
        for (int i = 0; i < 50; i++) {
            BusiThread busiThread = new BusiThread();
            busiThread.start();
        }

    }

    /**
     * 连接池
     */
    public static final DBPoolSemaphore pool = new DBPoolSemaphore();

    private static class BusiThread extends Thread{
        @Override
        public void run() {
            // 随机数工具类 为了让每个线程持有连接的时间不一样
            Random random = new Random();
            long start = System.currentTimeMillis();
            try {
                Connection connection = pool.takeConnection();
                System.out.println("Thread_"+Thread.currentThread().getId()+
                        "_获取数据库连接耗时["+(System.currentTimeMillis()-start)+"]ms.");
                // 模拟使用连接查询数据
                SmallTool.sleepMillis(100+random.nextInt(100));
                System.out.println("查询数据完成归还连接");
                pool.returnConnection(connection);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class SqlConnection implements Connection {

        /**
         * 获取数据库连接
         */
        public static Connection fetchConnection(){
            return new SqlConnection();
        }

        @Override
        public Statement createStatement()  {
            SmallTool.sleepMillis(1);
            return null;
        }

        @Override
        public PreparedStatement prepareStatement(String sql)  {
            return null;
        }

        @Override
        public CallableStatement prepareCall(String sql)  {
            return null;
        }

        @Override
        public String nativeSQL(String sql)  {
            return null;
        }

        @Override
        public void setAutoCommit(boolean autoCommit)  {

        }

        @Override
        public boolean getAutoCommit()  {
            return false;
        }

        @Override
        public void commit()  {
            SmallTool.sleepMillis(70);
        }

        @Override
        public void rollback()  {

        }

        @Override
        public void close()  {

        }

        @Override
        public boolean isClosed()  {
            return false;
        }

        @Override
        public DatabaseMetaData getMetaData()  {
            return null;
        }

        @Override
        public void setReadOnly(boolean readOnly)  {

        }

        @Override
        public boolean isReadOnly()  {
            return false;
        }

        @Override
        public void setCatalog(String catalog)  {

        }

        @Override
        public String getCatalog()  {
            return null;
        }

        @Override
        public void setTransactionIsolation(int level)  {

        }

        @Override
        public int getTransactionIsolation()  {
            return Connection.TRANSACTION_NONE;
        }

        @Override
        public SQLWarning getWarnings()  {
            return null;
        }

        @Override
        public void clearWarnings()  {

        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency)  {
            return null;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)  {
            return null;
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)  {
            return null;
        }

        @Override
        public Map<String, Class<?>> getTypeMap()  {
            return null;
        }

        @Override
        public void setTypeMap(Map<String, Class<?>> map)  {

        }

        @Override
        public void setHoldability(int holdability)  {

        }

        @Override
        public int getHoldability()  {
            return 0;
        }

        @Override
        public Savepoint setSavepoint()  {
            return null;
        }

        @Override
        public Savepoint setSavepoint(String name)  {
            return null;
        }

        @Override
        public void rollback(Savepoint savepoint)  {

        }

        @Override
        public void releaseSavepoint(Savepoint savepoint)  {

        }

        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)  {
            return null;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)  {
            return null;
        }

        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)  {
            return null;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)  {
            return null;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes)  {
            return null;
        }

        @Override
        public PreparedStatement prepareStatement(String sql, String[] columnNames)  {
            return null;
        }

        @Override
        public Clob createClob()  {
            return null;
        }

        @Override
        public Blob createBlob()  {
            return null;
        }

        @Override
        public NClob createNClob()  {
            return null;
        }

        @Override
        public SQLXML createSQLXML()  {
            return null;
        }

        @Override
        public boolean isValid(int timeout)  {
            return false;
        }

        @Override
        public void setClientInfo(String name, String value)  {

        }

        @Override
        public void setClientInfo(Properties properties)  {

        }

        @Override
        public String getClientInfo(String name)  {
            return null;
        }

        @Override
        public Properties getClientInfo()  {
            return null;
        }

        @Override
        public Array createArrayOf(String typeName, Object[] elements)  {
            return null;
        }

        @Override
        public Struct createStruct(String typeName, Object[] attributes)  {
            return null;
        }

        @Override
        public void setSchema(String schema)  {

        }

        @Override
        public String getSchema()  {
            return null;
        }

        @Override
        public void abort(Executor executor)  {

        }

        @Override
        public void setNetworkTimeout(Executor executor, int milliseconds)  {

        }

        @Override
        public int getNetworkTimeout()  {
            return 0;
        }

        @Override
        public <T> T unwrap(Class<T> iface)  {
            return null;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface)  {
            return false;
        }
    }

    /**
     * 使用信号量控制数据库的链接和释放
     */
    static class DBPoolSemaphore {

        /**
         * 池容量
         */
        private final static int POOL_SIZE = 10;

        /**
         * useful 代表可用连接
         * useless 代表已用连接
         *  为什么要使用两个Semaphore呢?是因为,在连接池中不只有连接本身是资源,空位也是资源,也需要记录
         */
        private final Semaphore useful, useless;

        /**
         * 连接池
         */
        private final static LinkedList<Connection> POOL = new LinkedList<>();

        /*
         * 使用静态块初始化池
         */
        static {
            for (int i = 0; i < POOL_SIZE; i++) {
                POOL.addLast(SqlConnection.fetchConnection());
            }
        }

        public DBPoolSemaphore() {
            // 初始可用的许可证等于池容量
            useful = new Semaphore(POOL_SIZE);
            // 初始不可用的许可证容量为0
            useless = new Semaphore(0);
        }

        /**
         * 获取数据库连接
         *
         * @return 连接对象
         */
        public Connection takeConnection() throws InterruptedException {
            // 可用许可证减一
            useful.acquire();
            Connection connection;
            synchronized (POOL) {
                connection = POOL.removeFirst();
            }
            // 不可用许可证数量加一
            useless.release();
            return connection;
        }

        /**
         * 释放链接
         *
         * @param connection 连接对象
         */
        public void returnConnection(Connection connection) throws InterruptedException {
            if(null!=connection){
                // 打印日志
                System.out.println("当前有"+useful.getQueueLength()+"个线程等待获取连接,,"
                        +"可用连接有"+useful.availablePermits()+"个");
                // 不可用许可证减一
                useless.acquire();
                synchronized (POOL){
                    POOL.addLast(connection);
                }
                // 可用许可证加一
                useful.release();
            }
        }

    }
}
