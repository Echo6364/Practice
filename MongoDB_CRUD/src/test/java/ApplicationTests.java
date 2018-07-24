import com.mongo.demo.DemoApplication;
import com.mongo.demo.model.User;
import com.mongo.demo.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DemoApplication.class)
public class ApplicationTests {

    @Autowired
    private UserService userService;

    @Test
    public void boot_mongo() {
        //添加三条user
        userService.insert(new User(178865, "zard", 18));
        userService.insert(new User(178899, "mr.", 20));
        userService.insert(new User(178900, "y.", 20));

        //查询单个user
        System.out.println(userService.getUser(178865));

        //插入一个user集合
        List<User> list = new ArrayList<>();
        for (int i = 10; i <= 15; i++) {
            list.add(new User( i ,""+i, i ));
        }
        userService.insertAll(list);

        //查询所有user
        System.out.println(userService.findAll());

        //删除一条user
        userService.remove(178865);

        //更新一条信息
        userService.update(new User( 178899 , "mr.",99));
    }

}