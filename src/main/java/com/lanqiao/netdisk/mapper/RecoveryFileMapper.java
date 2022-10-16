package com.lanqiao.netdisk.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lanqiao.netdisk.model.RecoveryFile;
import com.lanqiao.netdisk.vo.RecoveryFileListVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RecoveryFileMapper extends BaseMapper<RecoveryFile> {
    List<RecoveryFileListVO> selectRecoveryFileList();
}
