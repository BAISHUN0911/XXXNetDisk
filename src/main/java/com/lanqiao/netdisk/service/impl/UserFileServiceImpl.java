package com.lanqiao.netdisk.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanqiao.netdisk.constant.FileConstant;
import com.lanqiao.netdisk.mapper.UserFileMapper;
import com.lanqiao.netdisk.model.UserFile;
import com.lanqiao.netdisk.service.UserFileService;
import com.lanqiao.netdisk.vo.UserfileListVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Service
public class UserFileServiceImpl extends ServiceImpl<UserFileMapper, UserFile> implements UserFileService {

    @Resource
    UserFileMapper userFileMapper;


    @Override
    public List<UserfileListVO> getUserFileByFilePath(String filePath, Long userId, Long currentPage, Long pageCount) {
        Long beginCount = (currentPage-1)*pageCount;
        UserFile userFile = new UserFile();
        userFile.setUserId(userId);
        userFile.setFilePath(filePath);
        System.out.println("userFile"+userFile+"beginCount"+beginCount+"pageCount"+pageCount);
        List<UserfileListVO> fileList = userFileMapper.userfileList(userFile, beginCount, pageCount);
        return fileList;
    }

    @Override
    public Map<String, Object> getUserFileByType(int fileType, Long currentPage, Long pageCount, Long userId) {
        Long beginCount = (currentPage-1)*pageCount;
        List<UserfileListVO> fileList;
        Long total;
        if(fileType== FileConstant.OTHER_TYPE){
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.addAll(Arrays.asList(FileConstant.DOC_FILE));
            arrayList.addAll(Arrays.asList(FileConstant.IMG_FILE));
            arrayList.addAll(Arrays.asList(FileConstant.VIDEO_FILE));
            arrayList.addAll(Arrays.asList(FileConstant.MUSIC_FILE));
            userFileMapper.selectFileNotInExtendNames(arrayList,beginCount,pageCount,userId);
            total=userFileMapper.selectCountNotInExtendNames(arrayList,beginCount,pageCount,userId);

        }else {
            List<String> fileExtends = null;
            if(fileType==FileConstant.IMAGE_TYPE){
                fileExtends=Arrays.asList(FileConstant.IMG_FILE);
            }else if (fileType == FileConstant.DOC_TYPE) {
                fileExtends = Arrays.asList(FileConstant.DOC_FILE);
            } else if (fileType == FileConstant.VIDEO_TYPE) {
                fileExtends = Arrays.asList(FileConstant.VIDEO_FILE);
            } else if (fileType == FileConstant.MUSIC_TYPE) {
                fileExtends = Arrays.asList(FileConstant.MUSIC_FILE);
            }
            fileList=userFileMapper.selectFileByExtendName(fileExtends,beginCount,pageCount,userId);
            total=userFileMapper.selectCountByExtendName(fileExtends, beginCount, pageCount,userId);
            HashMap<String, Object> map = new HashMap<>();
            map.put("list",fileList);
            map.put("total",total);
            return map;

        }
        return null;
    }



}
