package com.lanqiao.netdisk.operation.download;

import com.lanqiao.netdisk.operation.download.domain.DownloadFile;

import javax.servlet.http.HttpServletResponse;

/**
 *
 */
public abstract class Downloader {

    public abstract void download(HttpServletResponse httpServletResponse, DownloadFile downloadFile);

}
