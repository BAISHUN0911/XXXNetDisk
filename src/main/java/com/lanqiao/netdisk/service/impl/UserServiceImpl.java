package com.lanqiao.netdisk.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanqiao.netdisk.common.RestResult;
import com.lanqiao.netdisk.mapper.UserMapper;
import com.lanqiao.netdisk.model.User;
import com.lanqiao.netdisk.service.UserService;
import com.lanqiao.netdisk.util.DateUtil;
import com.lanqiao.netdisk.util.JWTUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;


import java.util.List;
import java.util.UUID;


@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    JWTUtil jwtUtil;

    @Override
    public RestResult<String> registerUser(User user) {
        //判断验证码
        String telephone = user.getTelephone();
        String password = user.getPassword();
        if(!StringUtils.hasLength(telephone) || !StringUtils.hasLength(password)){
            return RestResult.fail().message("手机号或密码不能为空");
        }
        if (telephone.length() != 11){
            return RestResult.fail().message("请输入11位手机号");
        }
        if(isTelePhoneExit(telephone)){
            return RestResult.fail().message("手机号已存在");
        }

        String salt = UUID.randomUUID().toString().replace("-","").substring(15);//把用户注册的密码加盐值，盐值随机生成
        String passwordAndSalt = password + salt;
        String newPassword = DigestUtils.md5DigestAsHex(passwordAndSalt.getBytes());

        user.setSalt(salt);
        user.setPassword(newPassword);
        user.setRegisterTime(DateUtil.getCurrentTime());
        int result = userMapper.insert(user);

        if (result==1){
            return RestResult.success();
        }else {
            return RestResult.fail().message("注册用户失败，请检查输入信息");
        }

    }

    @Override
    public RestResult<User> login(User user) {
        String telephone = user.getTelephone();
        String password = user.getPassword();
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getTelephone, telephone);
        User saveUser = userMapper.selectOne(lambdaQueryWrapper);

        String salt = saveUser.getSalt();
        String passwordAndSalt = password + salt;
        String newPassword = DigestUtils.md5DigestAsHex(passwordAndSalt.getBytes());
        if (newPassword.equals(saveUser.getPassword())){    //与数据库中存储的该用户的密码比对
            saveUser.setPassword("");
            saveUser.setSalt("");
            return RestResult.success().data(saveUser);
        }else {
            return RestResult.fail().message("手机号或密码错误");
        }
    }

    @Override
    public User getUserByToken(String token) {
        User tokenUserInfo = null;
        try {
            Claims claims = jwtUtil.parseJWT(token);
            String subject = claims.getSubject();
            ObjectMapper mapper = new ObjectMapper();
            tokenUserInfo = mapper.readValue(subject, User.class);
        } catch (Exception e) {
            log.error("token解码异常");
            return null;    //如果返回 null，则认为 token 是无效的
        }
        return tokenUserInfo;
    }

    /**
     *
     * 检测手机号是否已经存在
     */
    private boolean isTelePhoneExit(String telephone){
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getTelephone, telephone);
        List<User> list = userMapper.selectList(lambdaQueryWrapper);
        if(list!=null&&!list.isEmpty()){
            return true;
        }else {
            return false;
        }
    }


}
