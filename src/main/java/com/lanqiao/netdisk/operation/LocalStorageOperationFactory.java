package com.lanqiao.netdisk.operation;

import com.lanqiao.netdisk.operation.delete.Deleter;
import com.lanqiao.netdisk.operation.delete.product.LocalStorageDeleter;
import com.lanqiao.netdisk.operation.download.Downloader;
import com.lanqiao.netdisk.operation.download.product.LocalStorageDownloader;
import com.lanqiao.netdisk.operation.upload.Uploader;
import com.lanqiao.netdisk.operation.upload.product.LocalStorageUploader;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 本地存储网盘文件工厂，一个具体的工厂
 * 以后还可以拓展阿里OSS存储方式工厂
 */
@Component
public class LocalStorageOperationFactory implements FileOperationFactory{

    @Resource
    LocalStorageUploader localStorageUploader;
    @Resource
    LocalStorageDownloader localStorageDownloader;
    @Resource
    LocalStorageDeleter localStorageDeleter;

    @Override
    public Uploader getUploader() {
        return localStorageUploader;
    }

    @Override
    public Downloader getDownloader() {
        return localStorageDownloader;
    }

    @Override
    public Deleter getDeleter() {
        return localStorageDeleter;
    }
}
