package com.lanqiao.netdisk.operation.delete.domain;

import lombok.Data;

/**
 * 删除文件实体类
 */
@Data
public class DeleteFile {
    private String fileUrl;
    private String timeStampName;
}
