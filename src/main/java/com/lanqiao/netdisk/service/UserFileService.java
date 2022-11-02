package com.lanqiao.netdisk.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.lanqiao.netdisk.model.UserFile;
import com.lanqiao.netdisk.vo.UserfileListVO;

import java.util.List;
import java.util.Map;

public interface UserFileService extends IService<UserFile> {

    //根据路径获取用户文件
    List<UserfileListVO> getUserFileByFilePath(String filePath, Long userId, Long currentPage, Long pageCount);
    //根据文件类型获取文件
    Map<String,Object> getUserFileByType(int fileType,Long currentPage,Long pageCount,Long userId);

    //删除文件
    void deleteUserFile(Long userFileId, Long sessionUserId);

    //根据一个文件路径获取文件树
    List<UserFile> selectFileTreeListLikeFilePath(String filePath, long userId);
    //通过用户id获取该用户的文件树
    List<UserFile> selectFilePathTreeByUserId(Long userId);

    //更改用户文件路径
    void updateFilepathByFilepath(String oldfilePath, String newfilePath, String fileName, String extendName, Long userId);

    //根据文件名和路径来查找文件,用来检测想要更新的文件名是否已存在
    List<UserFile> selectUserFileByNameAndPath(String fileName, String filePath, Long userId);

    void replaceUserFilePath(String filePath, String oldFilePath, Long userId);
}
