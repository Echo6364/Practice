package com.mongo.demo.daoimpl;

import com.mongo.demo.dao.UserDao;
import com.mongo.demo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

//Dao接口实现

@Repository("userDao")
public class UserDaoImpl implements UserDao {
    //使用 mongotemplate 模板
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 查询所有id
     * @return
     */
    @Override
    public List<User> findAll() {
        return mongoTemplate.findAll(User.class);
    }

    /**
     * 根据单个id查询
     * @param id
     * @return
     */

    @Override
    public User getUser(Integer id) {
        return mongoTemplate.findOne(new Query(Criteria.where("id").is(id)), User.class);
    }

    /**
     * 更新一条user
     * @param user
     */
    @Override
    public void update(User user) {
        Criteria criteria = Criteria.where("id").is(user.getId());
        Query query = new Query(criteria);
        Update update = Update.update("name", user.getName()).set("age" , user.getAge());
        mongoTemplate.updateMulti(query, update, User.class);
    }

    /**
     * 插入单条信息
     * @param user
     */
    @Override
    public void insert(User user) {
        mongoTemplate.insert(user);
    }

    /**
     * 插入集合
     * @param users
     */
    @Override
    public void insertAll(List<User> users) {
        mongoTemplate.insertAll(users);
    }

    /**
     * 根据id删除
     * @param id
     */
    @Override
    public void remove(Integer id) {
        Criteria criteria = Criteria.where("id").is(id);
        Query query = new Query(criteria);
        mongoTemplate.remove(query, User.class);
    }

    /**
     * 分页查询
     * @param user
     * @param pageable
     * @return
     */
    @Override
    public List<User> findByPage(User user, Pageable pageable) {
        Query query = new Query();
        if (user != null && user.getName() != null) {
            query = new Query(Criteria.where("name").regex("^" + user.getName()));
        }
        List<User> list = mongoTemplate.find(query.with(pageable), User.class);
        return list;
    }


}
