package com.lanqiao.netdisk.service;


import com.lanqiao.netdisk.dto.DownloadFileDTO;
import com.lanqiao.netdisk.dto.UploadFileDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface FiletransferService {

    void uploadFile(HttpServletRequest request, UploadFileDTO uploadFileDTO, Long userId);

    void downloadFile(HttpServletResponse response, DownloadFileDTO downloadFileDTO);

}
