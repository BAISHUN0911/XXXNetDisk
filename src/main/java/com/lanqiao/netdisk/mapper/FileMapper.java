package com.lanqiao.netdisk.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lanqiao.netdisk.model.File;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileMapper extends BaseMapper<File> {
}
