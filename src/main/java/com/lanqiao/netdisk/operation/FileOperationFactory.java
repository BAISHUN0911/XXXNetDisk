package com.lanqiao.netdisk.operation;

import com.lanqiao.netdisk.operation.delete.Deleter;
import com.lanqiao.netdisk.operation.download.Downloader;
import com.lanqiao.netdisk.operation.upload.Uploader;

/**
 * 文件操作工厂 顶层抽象工厂
 */
public interface FileOperationFactory {
    Uploader getUploader();
    Downloader getDownloader();
    Deleter getDeleter();
}
