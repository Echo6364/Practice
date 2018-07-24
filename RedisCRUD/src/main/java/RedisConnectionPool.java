import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Redis连接池
 */
public class RedisConnectionPool {
    private static String IP = "localhost";
    //IP 本地服务器
    private static int PORT = 6379;
    //端口
    private static int TIMEOUT = 10000;
    //超时 单位ms
    private static int MAX_ACTIVE = 8;
    //控制一个pool最大活动的jedis实例，默认值为8；
    private static int MAX_IDLE = 8;
    //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
    private static int MAX_WAIT = 10000;
    //连接最大等待时间，单位毫秒，默认值为-1，即会一直尝试连接redis。设置最大等待时间后，如果超时，则JedisConnectionException
    private static boolean TEST_ON_BORROW = true;
    //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
    private static JedisPool jedisPool = null;

    /**
     *初始化连接池
     */
    static {
        try {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(MAX_ACTIVE);
            //高版本以弃用setMaxActive
            config.setMaxIdle(MAX_IDLE);
            config.setMaxWaitMillis(MAX_WAIT);
            //高版本以弃用setMax
            config.setTestOnBorrow(TEST_ON_BORROW);
            jedisPool = new JedisPool(config, IP, PORT, TIMEOUT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取Jedis实例
     *
     * @return
     */

    public synchronized static Jedis getJedis() {
        try {
            if (jedisPool != null) {
                Jedis resource = jedisPool.getResource();
                return resource;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 释放jedis资源
     *
     * @param jedis
     */
    public static void CloseJedis(final Jedis jedis) {
        jedis.close();
    }
}

