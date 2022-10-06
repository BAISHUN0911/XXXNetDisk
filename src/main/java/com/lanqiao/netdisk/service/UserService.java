package com.lanqiao.netdisk.service;


import com.lanqiao.netdisk.common.RestResult;
import com.lanqiao.netdisk.model.User;

public interface UserService {

    RestResult<String> registerUser(User user);

    RestResult<User> login(User user);

    User getUserByToken(String token);

}
