package com.lanqiao.netdisk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * dto 用来存放创建文件参数
 */

@Schema(description = "创建文件DTO",required = true)
@Data
public class CreateFileDTO {

    @Schema(description = "文件名")
    private String fileName;
    @Schema(description = "文件路径")
    private String filePath;


}
