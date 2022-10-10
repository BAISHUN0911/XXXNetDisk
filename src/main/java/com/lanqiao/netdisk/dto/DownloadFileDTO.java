package com.lanqiao.netdisk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 下载文件 DTO
 */
@Data
@Schema(name = "下载文件DTO",required = true)
public class DownloadFileDTO {
    private Long userFileId;    //只需要userfile表中的主键：用户文件id，也就是userFileId
}
