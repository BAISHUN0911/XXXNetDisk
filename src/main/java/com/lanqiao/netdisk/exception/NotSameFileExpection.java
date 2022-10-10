package com.lanqiao.netdisk.exception;

/**
 * 文件 md5 校验失败异常
 */
public class NotSameFileExpection extends Exception{
    public NotSameFileExpection(){
        super("File MD5 Different");
    }
}
