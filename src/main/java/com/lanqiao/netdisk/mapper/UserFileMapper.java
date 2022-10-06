package com.lanqiao.netdisk.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lanqiao.netdisk.model.UserFile;
import com.lanqiao.netdisk.vo.UserfileListVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserFileMapper extends BaseMapper<UserFile> {

    //文件列表查询
    List<UserfileListVO> userfileList (
            UserFile userFile,
            Long beginCount,
            Long pageCount
            );

    /**
     *        在已知扩展名中查询文件
     * @param fileNameList
     * @param beginCount
     * @param pageCount
     * @param userId
     * @return
     */
    List<UserfileListVO> selectFileByExtendName(List<String> fileNameList,Long beginCount,Long pageCount,long userId);


    /**
     *        查询未知扩展名的文件
     * @param fileNameList
     * @param beginCount
     * @param pageCount
     * @param userId
     * @return
     */
    List<UserfileListVO> selectFileNotInExtendNames(List<String> fileNameList, Long beginCount, Long pageCount, long userId);

    Long selectCountNotInExtendNames(List<String> fileNameList, Long beginCount, Long pageCount, long userId);

    Long selectCountByExtendName(List<String> fileNameList, Long beginCount, Long pageCount, long userId);

}
