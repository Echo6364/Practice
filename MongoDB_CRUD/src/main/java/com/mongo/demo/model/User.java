package com.mongo.demo.model;

import org.springframework.data.annotation.Id;

import java.io.Serializable;

/**
 *User的实体类
 */
public class User implements Serializable {
    //序列化UID
    private static final long serialVersionUID = 1L;
    @Id
    private String _id;
    private int id;
    private String name;
    private int age;

    /**
     *
     * @param _id
     * @param id id值
     * @param name 名字
     * @param age 年龄
     */
    public User(String _id, int id, String name, int age) {
        this._id = _id;
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public User(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public User() {
        super();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String get_id() {
        return _id;
    }

    public void set_id() {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName() {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge() {
        this.age = age;
    }

    public String toString() {
        return "User [_id=" + _id + ", id=" + id + ", name=" + name + ", age=" + age + "]";
    }
}
