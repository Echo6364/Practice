package com.zabbix.demo;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * zabbix工具类
 *
 * @author
 *
 * 2018-8-2 17:18
 */


public class ZabbixUtil {
    // 常量
    private static String CONNURL = "http://10.255.71.102/zabbix/api_jsonrpc.php";
    private static String AUTH = null;
    private static final String USER = "Admin";
    private static final String PASSWORD = "zabbix";

    /**
     * 建立HTTP连接的post方法 post json
     * @param conUrl
     * @param map
     * @return
     */
    private static String doPost(String conUrl, Map map) {
        String param = JSON.toJSONString(map);
        HttpURLConnection connection = null;
        DataOutputStream out = null;
        BufferedReader reader = null;
        StringBuffer sb = null;
        try {
            //创建连接
            URL url = new URL(conUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json"); // 设置接收数据的格式
            connection.setRequestProperty("Content-Type", "application/json"); // 设置发送数据的格式

            connection.connect();

            //POST请求
            out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(param);
            out.flush();

            //读取响应
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String lines;
            sb = new StringBuffer("");
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(), "utf-8");
                sb.append(lines);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return sb.toString();

    }


    /**
     * 登录 zabbix
     */
    private static void loginAuth() {
        //Map
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("user", USER);
        params.put("password", PASSWORD);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("jsonrpc", "2.0");
        map.put("method", "user.login");
        map.put("params", params);
        map.put("auth", null);
        map.put("id", 0);

        String response = doPost(CONNURL, map);
        JSONObject json = JSON.parseObject(response);
        AUTH = json.getString("result");
    }

    /**
     * 获取 api 时所需的AUTH值
     * @return
     */
    private static String getAuth() {
        if (AUTH == null) {
            loginAuth();
        }
        return AUTH;
    }

    /**
     * 根据host名称获取host 如linuxServer
     * @param host
     * @return
     */
    public static String getHostIdByHostName(String host) {
        Map<String, Object> filter = new HashMap<String, Object>();
        filter.put("host", host);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("output", "hostid");
        params.put("filter", filter);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("jsonrpc", "2.0");
        map.put("method", "host.get");
        map.put("params", params);
        map.put("auth", getAuth());
        map.put("id", 0);
        String response = doPost(CONNURL, map);

        JSONArray result = JSON.parseObject(response).getJSONArray("result");
        if (result.size() > 0) {
            JSONObject json = result.getJSONObject(0);
            String hostid = json.getString("hostid");
            return hostid;
        } else {
            return null;
        }
    }

    /**
     * 根据host ip 获得hostid
     * @param ip
     * @return
     */
    public static String getHostIdByIp(String ip) {
        Map<String, Object> filter = new HashMap<String, Object>();
        filter.put("ip", ip);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("output", "extend");
        params.put("filter", filter);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("jsonrpc", "2.0");
        map.put("method", "host.get");
        map.put("params", params);
        map.put("auth", getAuth());
        map.put("id", 0);
        String response = doPost(CONNURL, map);

        JSONArray result = JSON.parseObject(response).getJSONArray("result");
        if (result.size() > 0) {
            JSONObject json = result.getJSONObject(0);
            String hostid = json.getString("hostid");
            return hostid;
        } else {
            return null;
        }
    }

    /**
     * 根据 host 和key 获得itemId
     * @param hostId
     * @param key
     * @return
     */
    public static JSONArray getItemByHostAndKey(String hostId, String key) {
        if (hostId == null) {
            return null;
        }
        Date start = new Date();
        Map<String, Object> search = new HashMap<String, Object>();
        search.put("key_", key);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("output", "extend");
        params.put("hostids", hostId);
        params.put("search", search);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("jsonrpc", "2.0");
        map.put("method", "item.get");
        map.put("params", getAuth());
        map.put("id", 0);

        String response = doPost(CONNURL, map);
        JSONArray result = JSON.parseObject(response).getJSONArray("result");
        return result;
    }

    /**
     * @param hostId
     * @param key
     * @return
     * @throws Exception
     */
    public static String getItemId(String hostId, String key) throws Exception {
        JSONArray result = getItemByHostAndKey(hostId, key);
        if (result != null) {
            if (result.size() > 0) {
                JSONObject json = result.getJSONObject(0);
                if (json != null) {
                    return json.getString("itemId");
                }
            }
        }
        return null;
    }

    /**
     * @param hostId
     * @param key
     * @return
     * @throws Exception
     */
    public static int getValueType(String hostId, String key) throws Exception {
        JSONArray result = getItemByHostAndKey(hostId, key);
        if (result != null) {
            if (result.size() > 0) {
                JSONObject json = result.getJSONObject(0);
                if (json != null) {
                    return json.getIntValue("value_type");
                }
            }
        }
        return 3;
    }

    /**
     * 根据时间 itemID 获取相关历史记录   limit方法，限定返回几组数据
     * @param itemId
     * @param valueType
     * @param from
     * @param to
     * @return
     */
    public static JSONArray getHistoryDataByItemId(String itemId, int valueType, long from, long to) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("output", "extend");
        params.put("history", valueType);
        params.put("itemids", itemId);
        params.put("sortfield", "clock");
        params.put("time_from", from / 1000);
        params.put("time_till", to / 1000);
//        params.put("limit", "10");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("jsonrpc", "2.0");
        map.put("method", "history.get");
        map.put("params", params);
        map.put("auth", getAuth());
        map.put("id", 1);
        String response = doPost(CONNURL, map);
        JSONArray result = JSON.parseObject(response).getJSONArray("result");
        return result;
    }

    public static void main(String[] args) throws Exception {
        loginAuth();
        long time_till = new Date().getTime();
        long time_from = time_till - 1000000;
        String ip = "10.255.66.6";
        String key = "system.cpu.util[,iowait]";
        String hostId = getHostIdByIp(ip);
        String itemId = getItemId(hostId, key);
        int valueType = getValueType(hostId, key);
        System.out.println(getAuth());
        System.out.println(hostId);
        System.out.println(itemId);
        System.out.println(valueType);
        System.out.println(getHistoryDataByItemId("3905", valueType, time_from, time_till));
    }
}
