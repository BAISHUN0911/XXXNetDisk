package com.lanqiao.netdisk.operation.upload.domain;

import lombok.Data;

/**
 * 上传文件实体
 */
@Data
public class UploadFile {
    private String fileName;        //文件名
    private String fileType;        //文件扩展名
    private long fileSize;          //文件大小
    private String timeStampName;   //时间戳名称
    private int success;        //1代表成功，0代表失败
    private String message;     //上传失败或成功的信息
    private String url;         //文件保存路径
    //切片上传相关参数
    private String taskId;
    private int chunkNumber;        //切片数量
    private long chunkSize;         //切片大小
    private int totalChunks;        //所有切片
    private String identifier;      //md5码
    private long totalSize;         //总大小
    private long currentChunkSize;      //当前切片大小
}
