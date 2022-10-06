package com.lanqiao.netdisk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * dto 用来存放接口请求参数
 */

@Data
@Schema(name = "文件列表DTO",required = true)
public class UserfileListDTO {
    @Schema(description = "文件路径")
    private String filePath;
    @Schema(description = "当前页码")
    private Long currentPage;
    @Schema(description = "一页显示数量")
    private Long pageCount;
}
