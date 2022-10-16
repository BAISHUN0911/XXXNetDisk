package com.lanqiao.netdisk.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.lanqiao.netdisk.model.UserFile;
import com.lanqiao.netdisk.vo.UserfileListVO;

import java.util.List;
import java.util.Map;

public interface UserFileService extends IService<UserFile> {

    List<UserfileListVO> getUserFileByFilePath(String filePath, Long userId, Long currentPage, Long pageCount);


    Map<String,Object> getUserFileByType(int fileType,Long currentPage,Long pageCount,Long userId);

    void deleteUserFile(Long userFileId, Long sessionUserId);

    List<UserFile> selectFileTreeListLikeFilePath(String filePath, long userId);
}
