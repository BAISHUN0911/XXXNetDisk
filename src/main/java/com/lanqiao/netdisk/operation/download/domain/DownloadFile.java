package com.lanqiao.netdisk.operation.download.domain;

import lombok.Data;

/**
 * 本地下载文件实体类
 */
@Data
public class DownloadFile {
    private String fileUrl;
    private String timeStampName;   //时间戳
}
