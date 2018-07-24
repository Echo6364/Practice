package com.mongo.demo.dao;

import com.mongo.demo.model.User;
import org.springframework.data.domain.Pageable;

import java.util.List;

//Dao接口类
public interface UserDao {
    //查询所有User
    List<User> findAll();
    //根据id查询
    User getUser(Integer id);
    //更新user信息
    void update(User user);
    //插入user信息
    void insert(User user);
    //插入多个
    void insertAll(List<User> users);
    //根据id删除user内容
    void remove(Integer id);
    List<User> findByPage(User user, Pageable pageable);
}
