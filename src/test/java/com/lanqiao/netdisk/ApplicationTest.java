package com.lanqiao.netdisk;

import com.lanqiao.netdisk.common.RestResult;
import com.lanqiao.netdisk.mapper.UserMapper;
import com.lanqiao.netdisk.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;

@SpringBootTest
public class ApplicationTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void test1() {
        User user = new User();
        user.setUsername("用户名1");
        user.setPassword("密码1");
        user.setTelephone("手机号1");
        userMapper.insertUser(user);
        System.out.println("数据库字段查询结果显示");
        List<User> list = userMapper.selectUser();
        list.forEach(System.out::println);
    }

    @Test
    public void test2(){
        User user = new User();
        user.setUsername("用户名2");
        user.setPassword("密码2");
        user.setTelephone("手机号2");
        int i = userMapper.insert(user);
        System.out.println(i);      // 1 userMapper.insert(user)返回1代表用户添加成功
        List list = userMapper.selectList(null);
        System.out.println("数据库字段查询结果显示");
        list.forEach(System.out::println);
    }

    @Test
    public void test3(){
        Date date = new Date();
        System.out.println("格式化前的时间");
        System.out.println(date);
        System.out.println("格式化后的时间");
        String stringDate = String.format("%tF %<tT", date);
        System.out.println(stringDate);
        System.out.println(RestResult.success());
    }

    @Test
    public void testmd5DigestAsHex(){
        String newPassword = "72a922104bd8976465398445e35b6b86";
        byte[] bytes = DigestUtils.md5Digest(newPassword.getBytes());
        for (byte b : bytes) {
            System.out.print(b);
        }
        System.out.println();
        String a = "100";
        String b = "100";
        System.out.println(a+b);
    }
}
