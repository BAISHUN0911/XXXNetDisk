package com.lanqiao.netdisk.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanqiao.netdisk.constant.FileConstant;
import com.lanqiao.netdisk.mapper.FileMapper;
import com.lanqiao.netdisk.mapper.RecoveryFileMapper;
import com.lanqiao.netdisk.mapper.UserFileMapper;
import com.lanqiao.netdisk.model.File;
import com.lanqiao.netdisk.model.RecoveryFile;
import com.lanqiao.netdisk.model.UserFile;
import com.lanqiao.netdisk.service.UserFileService;
import com.lanqiao.netdisk.util.DateUtil;
import com.lanqiao.netdisk.vo.UserfileListVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class UserFileServiceImpl extends ServiceImpl<UserFileMapper, UserFile> implements UserFileService {

    public static Executor executor = Executors.newFixedThreadPool(20);         //线程池最大20

    @Resource
    UserFileMapper userFileMapper;
    @Resource
    FileMapper fileMapper;
    @Resource
    RecoveryFileMapper recoveryFileMapper;


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
        List<UserfileListVO> fileList;              //
        Long total;
        if(fileType== FileConstant.OTHER_TYPE){
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.addAll(Arrays.asList(FileConstant.DOC_FILE));
            arrayList.addAll(Arrays.asList(FileConstant.IMG_FILE));
            arrayList.addAll(Arrays.asList(FileConstant.VIDEO_FILE));
            arrayList.addAll(Arrays.asList(FileConstant.MUSIC_FILE));
            userFileMapper.selectFileNotInExtendNames(arrayList,beginCount,pageCount,userId);
            total=userFileMapper.selectCountNotInExtendNames(arrayList,beginCount,pageCount,userId);    //计算这类文件的数量，目前不知道啥用
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
            total=userFileMapper.selectCountByExtendName(fileExtends, beginCount, pageCount,userId);    //计算这类文件的数量
            HashMap<String, Object> map = new HashMap<>();
            map.put("list",fileList);
            map.put("total",total);
            return map;

        }
        return null;
    }

    @Override
    public void deleteUserFile(Long userFileId, Long sessionUserId) {
        UserFile userFile = userFileMapper.selectById(userFileId);
        String uuid = UUID.randomUUID().toString();             //用UUID作为删除文件的删除批次号
        if (userFile.getIsDir() == 1){  //这是一个目录，删除目录下所有文件
            LambdaUpdateWrapper<UserFile> userFileLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            userFileLambdaUpdateWrapper.set(UserFile::getDeleteFlag,1)
                    .set(UserFile::getDeleteBatchNum,uuid)
                    .set(UserFile::getDeleteTime, DateUtil.getCurrentTime())
                    .eq(UserFile::getUserFileId,userFileId);
            userFileMapper.update(null,userFileLambdaUpdateWrapper);
            String filePath = userFile.getFilePath()+userFile.getFileName()+"/";
            updateFileDeleteStateByFilePath(filePath,userFile.getDeleteBatchNum(),sessionUserId);
        }else {
            UserFile userFileTemp = userFileMapper.selectById(userFileId);
            File file = fileMapper.selectById(userFileTemp.getFileId());
            LambdaUpdateWrapper<UserFile> userFileLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            userFileLambdaUpdateWrapper.set(UserFile::getDeleteFlag,1)
                    .set(UserFile::getDeleteTime,DateUtil.getCurrentTime())
                    .eq(UserFile::getUserFileId,userFileTemp.getUserFileId());
            userFileMapper.update(null,userFileLambdaUpdateWrapper);
        }
        RecoveryFile recoveryFile = new RecoveryFile();         //将这次删除的文件添加到recoveryfile表记录
        recoveryFile.setUserFileId(userFileId);
        recoveryFile.setDeleteTime(DateUtil.getCurrentTime());
        recoveryFile.setDeleteBatchNum(uuid);
        recoveryFileMapper.insert(recoveryFile);
    }

    private void updateFileDeleteStateByFilePath(String filePath,String deleteBatchNum,Long userId){
        new Thread(()->{
            List<UserFile> fileList = selectFileTreeListLikeFilePath(filePath, userId);
            for (int i = 0; i < fileList.size(); i++) {
                UserFile userFileTemp = fileList.get(i);
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        //标记删除标志
                        LambdaUpdateWrapper<UserFile> userFileLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                        userFileLambdaUpdateWrapper.set(UserFile::getDeleteFlag,1)
                                .set(UserFile::getDeleteTime,DateUtil.getCurrentTime())
                                .set(UserFile::getDeleteBatchNum,deleteBatchNum)
                                .eq(UserFile::getUserFileId,userFileTemp.getUserFileId())
                                .eq(UserFile::getDeleteFlag,0);
                        userFileMapper.update(null,userFileLambdaUpdateWrapper);
                    }
                });
            }
        }).start();
    }

    @Override
    public List<UserFile> selectFileTreeListLikeFilePath(String filePath, long userId) {
        //UserFile userFile = new UserFile();
        filePath = filePath.replace("\\", "\\\\\\\\");
        filePath = filePath.replace("'", "\\'");
        filePath = filePath.replace("%", "\\%");
        filePath = filePath.replace("_", "\\_");

        //userFile.setFilePath(filePath);

        LambdaQueryWrapper<UserFile> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        log.info("查询文件路径：" + filePath);

        lambdaQueryWrapper.eq(UserFile::getUserId, userId).likeRight(UserFile::getFilePath, filePath);
        return userFileMapper.selectList(lambdaQueryWrapper);
    }

    @Override
    public List<UserFile> selectFilePathTreeByUserId(Long userId) {
        LambdaQueryWrapper<UserFile> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserFile::getUserId,userId)
                .eq(UserFile::getIsDir,1);
        return userFileMapper.selectList(lambdaQueryWrapper);
    }

    @Override
    public void updateFilepathByFilepath(String oldfilePath, String newfilePath, String fileName, String extendName, Long userId) {
        if ("null".equals(extendName)){
            extendName=null;
        }
        LambdaUpdateWrapper<UserFile> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(UserFile::getFilePath,newfilePath)
                .eq(UserFile::getFilePath,oldfilePath)
                .eq(UserFile::getFileName,fileName)
                .eq(UserFile::getUserId,userId);

        if (StringUtils.isNotEmpty(extendName)){
            lambdaUpdateWrapper.eq(UserFile::getExtendName,extendName);
        }else {
            lambdaUpdateWrapper.isNull(UserFile::getExtendName);
        }

        userFileMapper.update(null,lambdaUpdateWrapper);
        //移动子目录
        oldfilePath=oldfilePath+fileName+"/";
        newfilePath=newfilePath+fileName+"/";
        oldfilePath = oldfilePath.replace("\\", "\\\\\\\\");
        oldfilePath = oldfilePath.replace("'", "\\'");
        oldfilePath = oldfilePath.replace("%", "\\%");
        oldfilePath = oldfilePath.replace("_", "\\_");
        if (extendName==null){
            //没扩展名，一般是目录，需要移动子目录
            userFileMapper.updateFilepathByFilepath(oldfilePath,newfilePath,userId);
        }
    }

    @Override
    public List<UserFile> selectUserFileByNameAndPath(String fileName, String filePath, Long userId) {
        LambdaQueryWrapper<UserFile> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserFile::getFileName,fileName)
                .eq(UserFile::getFilePath,filePath)
                .eq(UserFile::getUserId,userId)
                .eq(UserFile::getDeleteFlag,"0");
        return userFileMapper.selectList(lambdaQueryWrapper);
    }

    @Override
    public void replaceUserFilePath(String filePath, String oldFilePath, Long userId) {
        userFileMapper.replaceFilePath(filePath,oldFilePath,userId);
    }


}
