package com.lanqiao.netdisk.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanqiao.netdisk.common.RestResult;
import com.lanqiao.netdisk.dto.RegisterDTO;
import com.lanqiao.netdisk.model.User;
import com.lanqiao.netdisk.service.UserService;
import com.lanqiao.netdisk.util.JWTUtil;
import com.lanqiao.netdisk.vo.LoginVO;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "user", description = "该接口为用户接口，主要做用户登录，注册和校验token")
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;
    @Autowired
    JWTUtil jwtUtil;

    /**
     * 成功响应测试
     */
    @GetMapping(value="/test1")
    @ResponseBody
    public RestResult test1(){
        return RestResult.success();
    }

    /**
     * 失败响应测试
     */
    @GetMapping(value="/test2")
    @ResponseBody
    public RestResult test2(){
        return RestResult.fail();
    }

    /**
     * 空指针异常响应测试
     */
    @GetMapping(value="/test3")
    @ResponseBody
    public RestResult test3(){
        String s = null;
        int i = s.length();
        return RestResult.success();
    }

    @Operation(summary = "用户注册",description = "注册账号",tags = {"user"})
    @PostMapping(value = "/register")
    @ResponseBody
    public RestResult<String> register(@RequestBody RegisterDTO registerDTO) {
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setTelephone(registerDTO.getTelephone());
        user.setPassword(registerDTO.getPassword());
        RestResult<String> restResult = userService.registerUser(user);
        return restResult;
    }

    @Operation(summary = "用户登录",description = "用户登录认证后才能进入系统",tags = {"user"})
    @GetMapping(value = "/login")
    @ResponseBody
    public RestResult<LoginVO> userLogin(String telephone, String password) {
        RestResult<LoginVO> restResult = new RestResult<LoginVO>();
        LoginVO loginVO = new LoginVO();
        User user = new User();
        user.setTelephone(telephone);
        user.setPassword(password);
        RestResult<User> loginResult = userService.login(user);
        if (!loginResult.getSuccess()) {
            return RestResult.fail().message("登录失败！");
        }

        loginVO.setUsername(loginResult.getData().getUsername());
        String jwt="";
        try{
            ObjectMapper mapper = new ObjectMapper();
            System.out.println("userService.login返回的User对象"+loginResult.getData());
            jwt = jwtUtil.createJWT(mapper.writeValueAsString(loginResult.getData()));
        } catch (JsonProcessingException e) {
            return RestResult.fail().message("登录失败！");
        } catch (Exception e) {
            return RestResult.fail().message("登录失败！");
        }

        loginVO.setToken(jwt);
        return RestResult.success().data(loginVO);
    }

    @Operation(summary = "检查用户登陆token",description = "验证token有效性",tags = {"user"})
    @GetMapping("/checkuserlogininfo")
    @ResponseBody
    public RestResult<User> checkToken(@RequestHeader("token") String token) {
        //用户登录成功后，可以调用该接口来获取登录状态，判断 token 是否失效，保证前后台登录状态一致。
        RestResult<User> restResult = new RestResult<User>();
        User tokenUserInfo = null;
        try {
            Claims c = jwtUtil.parseJWT(token);
            String subject = c.getSubject();
            ObjectMapper objectMapper = new ObjectMapper();
            tokenUserInfo = objectMapper.readValue(subject, User.class);
        } catch (Exception e) {
            log.error("解码异常");
            return RestResult.fail().message("认证失败");
        }
        if (tokenUserInfo != null) {
            return RestResult.success().data(tokenUserInfo);
        } else {
            return RestResult.fail().message("用户暂未登录");
        }
    }

}
