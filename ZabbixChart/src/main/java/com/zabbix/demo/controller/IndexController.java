package com.zabbix.demo.controller;

import com.alibaba.fastjson.JSONArray;
import com.zabbix.demo.ZabbixUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Date;

@CrossOrigin
@RequestMapping
@Controller
public class IndexController {
    ZabbixUtil zabbixUtil = new ZabbixUtil();

    @ResponseBody
    @RequestMapping("/zabbixIndex")
    public String index(HttpServletResponse response) throws Exception {
        long time_till = new Date().getTime();
        long time_from = time_till - 3600000;
        PrintWriter out = response.getWriter();
        JSONArray jsonArray = zabbixUtil.getHistoryDataByItemId("3905", 3, time_from, time_till);
        out.write(jsonArray.toString());
        out.flush();
        out.close();
        return null;
    }
}
