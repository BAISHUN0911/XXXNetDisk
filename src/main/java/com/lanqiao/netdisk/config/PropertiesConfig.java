package com.lanqiao.netdisk.config;

import com.lanqiao.netdisk.util.PropertiesUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 用来读取环境变量
 */

@Configuration
public class PropertiesConfig {

    @Resource
    private Environment env;

    @PostConstruct
    public void setProperties(){
        PropertiesUtil.setEnvironment(env);
    }

}
