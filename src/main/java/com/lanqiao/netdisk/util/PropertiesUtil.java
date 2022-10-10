package com.lanqiao.netdisk.util;

import org.springframework.core.env.Environment;

/**
 * 通过该类的 getProperty 方法，可以获取 application.properties 中的配置
 */
public class PropertiesUtil {

    private static Environment env = null;

    public static void setEnvironment(Environment env){
        PropertiesUtil.env = env;
    }

    public static String getProperty(String key){
        return PropertiesUtil.env.getProperty(key);
    }

}
