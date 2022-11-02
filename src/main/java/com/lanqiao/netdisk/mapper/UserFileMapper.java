package com.lanqiao.netdisk.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lanqiao.netdisk.model.UserFile;
import com.lanqiao.netdisk.vo.UserfileListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserFileMapper extends BaseMapper<UserFile> {

    //文件列表查询，查询该用户所有文件
    List<UserfileListVO> userfileList (UserFile userFile, Long beginCount, Long pageCount);

    /**
     *        在已知扩展名中查询文件
     * @param fileNameList 文件名字列表
     * @param beginCount    用来分页，定位到哪一页
     * @param pageCount     每页显示的数据
     * @param userId        用户id
     * @return              查询到有扩展名的文件集合
     */
    List<UserfileListVO> selectFileByExtendName(List<String> fileNameList,Long beginCount,Long pageCount,long userId);


    /**
     *        查询未知扩展名的文件
     * @param fileNameList      文件名字列表
     * @param beginCount        用来分页，定位到哪一页
     * @param pageCount         每页显示的数据
     * @param userId            用户id
     * @return                  查询到没有扩展名的文件集合
     */
    List<UserfileListVO> selectFileNotInExtendNames(List<String> fileNameList, Long beginCount, Long pageCount, long userId);

    //没有扩展名文件的总数，大概率是文件夹
    Long selectCountNotInExtendNames(List<String> fileNameList, Long beginCount, Long pageCount, long userId);
    //有扩展名文件的总数
    Long selectCountByExtendName(List<String> fileNameList, Long beginCount, Long pageCount, long userId);

    //移动文件
    void updateFilepathByFilepath(String oldfilePath, String newfilePath, Long userId);

    void replaceFilePath(@Param("filePath") String filePath, @Param("oldFilePath") String oldFilePath, @Param("userId") Long userId);

    //根据用户id获取这个用户存储的文件总大小
    Long selectStorageSizeByUserId(@Param("userId") Long userId);
}
