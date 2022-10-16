package com.lanqiao.netdisk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 *批量删除文件接口参数
 */
@Data
@Schema(name = "批量删除文件DTO",required = true)
public class BatchDeleteFileDTO {
    @Schema(description="文件集合")
    private String files;
}