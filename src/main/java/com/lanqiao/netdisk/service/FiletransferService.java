package com.lanqiao.netdisk.service;


import com.lanqiao.netdisk.dto.DownloadFileDTO;
import com.lanqiao.netdisk.dto.UploadFileDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface FiletransferService {

    //上传文件
    void uploadFile(HttpServletRequest request, UploadFileDTO uploadFileDTO, Long userId);

    //下载文件
    void downloadFile(HttpServletResponse response, DownloadFileDTO downloadFileDTO);

    //获取用户已用空间
    Long selectStorageSizeByUserId(Long userId);


}
