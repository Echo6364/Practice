import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis增删改查 相关操作
 */
public class RedisOperation {
    private Jedis jedis = RedisConnectionPool.getJedis();

    /**
     * 普通的key的set操作
     * @param key
     * @param map
     * @return
     */
    public String set(String key, HashMap<String, String> map) {
        return jedis.hmset(key, map);
    }
    public String set(byte[] key, HashMap<byte[], byte[]> map) {
        return jedis.hmset(key, map);
    }

    /**
     * 队列的操作方法，首部插入
     * @param key
     * @param list
     * @return
     */
    public String set(String key, List<String> list) {
        Long aLong = -1l;
        for (String value : list) {
            aLong = jedis.lpush(key, value);
        }
        return aLong <= 0 ? "error" : "OK";
    }
    public String set(byte[] key, List<byte[]> list) {
        Long aLong = -1l;
        for (byte[] value : list
                ) {
            aLong = jedis.lpush(key, value);
        }
        return aLong <= 0 ? "error" : "OK";
    }

    public String set(String key, String value) {
        return jedis.set(key, value);
    }

    public String set(byte[] key, byte[] value) {
        return jedis.set(key, value);
    }

    public String set(String key, Set<String> set) {
        Long aLong = -1l;
        for (String value : set) {
            aLong = jedis.sadd(key, value);
        }
        return aLong <= 0 ? "error" : "OK";
    }

    /**
     * 集合的操作，将一个或者多个元素加入到集合的key中
     * @param key
     * @param Obj
     * @return
     */
    public String set(String key, Object Obj) {
        return jedis.set(key.getBytes(), SerializeUtil.serialize(Obj));
    }
    //redis写入对象，用序列化对象的方式
    public Object get(String key) {
        Object object = null;
        //获取value的类型
        String type = jedis.type(key);
        //根据不同类型调用不同的获取方法
        if (type.toLowerCase().equals("map")) {
            object = jedis.hgetAll(key);
        } else if (type.toLowerCase().equals("set")) {
            object = jedis.smembers(key);
        } else if (type.toLowerCase().equals("list")) {
            object = jedis.lrange(key, 0, -1);
        } else if (type.toLowerCase().equals("string")) {
            byte[] bytes = jedis.get(key.getBytes());
            object = SerializeUtil.unserialize(bytes);
            if (object == null) {
                object = jedis.get(key);
            }
        }
        return object;
    }

    public Object get(byte[] key) {
        return null;
    }

    public Long delete(String key) {
        return jedis.del(key);
    }

    public String update(String key, Object value) {
        //先删除原有数据
        Long along = jedis.del(key);
        if (along < 0) {
            return "error";
        }
        //再重新添加
        if (value instanceof Map) {
            set(key, (Map) value);
        } else if (value instanceof Set) {
            set(key, (Set) value);
        } else if (value instanceof List) {
            set(key, (List) value);
        } else if (value instanceof String) {
            set(key, (String) value);
        } else {
            set(key, value);
        }
        return "OK";
    }

}
