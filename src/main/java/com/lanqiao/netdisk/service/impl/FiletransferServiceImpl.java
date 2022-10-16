package com.lanqiao.netdisk.service.impl;


import com.lanqiao.netdisk.dto.DownloadFileDTO;
import com.lanqiao.netdisk.dto.UploadFileDTO;
import com.lanqiao.netdisk.mapper.FileMapper;
import com.lanqiao.netdisk.mapper.UserFileMapper;
import com.lanqiao.netdisk.model.File;
import com.lanqiao.netdisk.model.UserFile;
import com.lanqiao.netdisk.operation.FileOperationFactory;
import com.lanqiao.netdisk.operation.download.Downloader;
import com.lanqiao.netdisk.operation.download.domain.DownloadFile;
import com.lanqiao.netdisk.operation.upload.Uploader;
import com.lanqiao.netdisk.operation.upload.domain.UploadFile;
import com.lanqiao.netdisk.service.FiletransferService;
import com.lanqiao.netdisk.util.DateUtil;
import com.lanqiao.netdisk.util.PropertiesUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Service
public class FiletransferServiceImpl implements FiletransferService {

    @Resource
    FileMapper fileMapper;
    @Resource
    UserFileMapper userFileMapper;
    @Resource
    FileOperationFactory localStorageOperationFactory;

    @Override
    public void uploadFile(HttpServletRequest request, UploadFileDTO uploadFileDTO, Long userId) {
        Uploader uploader = null;
        UploadFile uploadFile = new UploadFile();

        uploadFile.setChunkNumber(uploadFileDTO.getChunkNumber());
        uploadFile.setChunkSize(uploadFileDTO.getChunkSize());
        uploadFile.setTotalChunks(uploadFileDTO.getTotalChunks());
        uploadFile.setIdentifier(uploadFileDTO.getIdentifier());
        uploadFile.setTotalSize(uploadFileDTO.getTotalSize());
        uploadFile.setCurrentChunkSize(uploadFileDTO.getCurrentChunkSize());

        String storageType = PropertiesUtil.getProperty("file.storage-type");   //获取文件存储类型。本地？OSS？
        synchronized (FiletransferService.class){
            if ("0".equals(storageType)){
                uploader = localStorageOperationFactory.getUploader();
            }
        }

        List<UploadFile> uploadFileList = uploader.upload(request, uploadFile);
        for (int i = 0; i < uploadFileList.size(); i++) {
            uploadFile = uploadFileList.get(i); //获取每一个分片
            File file = new File();
            file.setIdentifier(uploadFileDTO.getIdentifier());
            file.setStorageType(Integer.parseInt(storageType));
            file.setTimeStampName(uploadFile.getTimeStampName());
            if (uploadFile.getSuccess() == 1){
                file.setFileUrl(uploadFile.getUrl());
                file.setFileSize(uploadFile.getFileSize());
                file.setPointCount(1);
                fileMapper.insert(file);
                UserFile userFile = new UserFile();
                userFile.setFileId(file.getFileId());
                userFile.setExtendName(uploadFile.getFileType());
                userFile.setFileName(uploadFile.getFileName());
                userFile.setFilePath(uploadFileDTO.getFilePath());
                userFile.setDeleteFlag(0);
                userFile.setUserId(userId);
                userFile.setIsDir(0);
                userFile.setUploadTime(DateUtil.getCurrentTime());
                userFileMapper.insert(userFile);
            }
        }


    }

    @Override
    public void downloadFile(HttpServletResponse httpServletResponse, DownloadFileDTO downloadFileDTO) {
        UserFile userFile = userFileMapper.selectById(downloadFileDTO.getUserFileId());     //在userfile表中通过userFileId查询数据

        String fileName = userFile.getFileName() + "." + userFile.getExtendName();      //将文件名和文件扩展名拼接成完整的文件名
        try {
            fileName = new String(fileName.getBytes("utf-8"), "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        httpServletResponse.setContentType("application/force-download");// 设置强制下载不打开
        httpServletResponse.addHeader("Content-Disposition", "attachment;fileName=" + fileName);// 设置文件名


        File file = fileMapper.selectById(userFile.getFileId());        //数据库中：通过userfile中的fileId字段查找对应的file表中信息
        Downloader downloader = null;
        if (file.getStorageType() == 0) {           //存储类型为0代表本地存储，则使用localStorageOperationFactory这个本地存储方式工厂
            downloader = localStorageOperationFactory.getDownloader();
        }
        DownloadFile downloadFile = new DownloadFile();
        downloadFile.setFileUrl(file.getFileUrl());                         //通过file实体类获取文件url
        downloadFile.setTimeStampName(file.getTimeStampName());             //通过file实体类获取文件时间戳
        downloader.download(httpServletResponse, downloadFile);             //使用具体下载工厂的实现LocalStorageDownloader中方法进行下载
    }



}
