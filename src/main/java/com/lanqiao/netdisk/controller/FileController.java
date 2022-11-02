package com.lanqiao.netdisk.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lanqiao.netdisk.common.RestResult;
import com.lanqiao.netdisk.dto.*;
import com.lanqiao.netdisk.model.File;
import com.lanqiao.netdisk.model.User;
import com.lanqiao.netdisk.model.UserFile;
import com.lanqiao.netdisk.service.FileService;
import com.lanqiao.netdisk.service.UserFileService;
import com.lanqiao.netdisk.service.UserService;
import com.lanqiao.netdisk.util.DateUtil;
import com.lanqiao.netdisk.vo.TreeNodeVO;
import com.lanqiao.netdisk.vo.UserfileListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@Tag(name = "file", description = "该接口为文件接口，主要用来做一些文件的基本操作，如创建目录，删除，移动，复制等。")
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    FileService fileService;
    @Resource
    UserService userService;
    @Resource
    UserFileService userfileService;

    @Operation(summary = "创建文件", description = "目录(文件夹)的创建", tags = {"file"})
    @PostMapping(value = "/createfile")
    @ResponseBody
    public RestResult<String> createFile(@RequestBody CreateFileDTO createFileDto, @RequestHeader("token") String token) {
        User sessionUser = userService.getUserByToken(token);
        if(sessionUser==null){
            RestResult.fail().message("token认证失败");
        }
        LambdaQueryWrapper<UserFile> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserFile::getFileName,"").eq(UserFile::getFilePath,"").eq(UserFile::getUserId,0);
        List<UserFile> userFiles = userfileService.list(lambdaQueryWrapper);
        if(!userFiles.isEmpty()){
            RestResult.fail().message("同目录下文件名重复");
        }
        UserFile userFile = new UserFile();
        userFile.setUserId(sessionUser.getUserId());
        userFile.setFileName(createFileDto.getFileName());
        userFile.setFilePath(createFileDto.getFilePath());
        userFile.setIsDir(1);                       //创建的是文件夹
        userFile.setUploadTime(DateUtil.getCurrentTime());
        userFile.setDeleteFlag(0);                  //标志为0代表未删除
        userfileService.save(userFile);
        return RestResult.success();

    }


    @Operation(summary = "获取文件列表", description = "用来做前台文件列表展示", tags = { "file" })
    @GetMapping(value = "/getfilelist")
    @ResponseBody
    public RestResult<UserfileListVO> getUserfileList(UserfileListDTO userfileListDTO,@RequestHeader String token){
        User sessionUser = userService.getUserByToken(token);
        if(sessionUser==null){
            return RestResult.fail().message("token验证失败");
        }
        System.out.println(userfileListDTO.getFilePath()+"--"+sessionUser.getUserId()+"--"+userfileListDTO.getCurrentPage()+"--"+userfileListDTO.getPageCount());
        List<UserfileListVO> fileList = userfileService.getUserFileByFilePath(userfileListDTO.getFilePath(),
                sessionUser.getUserId(),
                userfileListDTO.getCurrentPage(),
                userfileListDTO.getPageCount()
                );
        System.out.println("fileList======================>"+fileList);
        LambdaQueryWrapper<UserFile> userFileLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userFileLambdaQueryWrapper.eq(UserFile::getUserId, sessionUser.getUserId())
                .eq(UserFile::getFilePath,userfileListDTO.getFilePath())
                .eq(UserFile::getDeleteFlag,0);
        int count = userfileService.count(userFileLambdaQueryWrapper);
        HashMap<String, Object> map = new HashMap<>();
        map.put("count",count);
        map.put("list",fileList);
        return RestResult.success().data(map);
    }

    @Operation(summary = "通过文件类型选择文件", description = "实现文件格式分类查看", tags = { "file" })
    @GetMapping(value = "/selectfilebyfiletype")
    @ResponseBody
    public RestResult<List<Map<String,Object>>> selectFileByFileType(int fileType, Long currentPage, Long pageCount, @RequestHeader("token") String token){
        User sessionUser = userService.getUserByToken(token);
        if(sessionUser==null){
            return RestResult.fail().message("token认证失败");
        }
        Long userId = sessionUser.getUserId();
        Map<String, Object> map = userfileService.getUserFileByType(fileType, currentPage, pageCount, userId);
        return RestResult.success().data(map);

    }

    @Operation(summary = "删除文件",description = "可以删除文件或者目录",tags = {"file"})
    @RequestMapping(value = "/deletefile",method = RequestMethod.POST)
    @ResponseBody
    public RestResult deleteFile(@RequestBody DeleteFileDTO deleteFileDTO,@RequestHeader("token")String token){
        User sessionUser = userService.getUserByToken(token);
        userfileService.deleteUserFile(deleteFileDTO.getUserFileId(), sessionUser.getUserId());
        return RestResult.success();
    }

    @Operation(summary = "批量删除文件", description = "批量删除文件", tags = { "file" })
    @RequestMapping(value = "/batchdeletefile", method = RequestMethod.POST)
    @ResponseBody
    public RestResult<String> deleteImageByIds(@RequestBody BatchDeleteFileDTO batchDeleteFileDto,
                                               @RequestHeader("token") String token) {
        User sessionUser = userService.getUserByToken(token);
        List<UserFile> userFiles = JSON.parseArray(batchDeleteFileDto.getFiles(), UserFile.class);
        for (UserFile userFile : userFiles) {
            userfileService.deleteUserFile(userFile.getUserFileId(), sessionUser.getUserId());
        }
        return RestResult.success().message("批量删除文件成功");
    }

    @Operation(summary = "获取文件树", description = "文件移动的时候需要用到该接口，用来展示目录树", tags = {"file"})
    @RequestMapping(value = "/getfiletree", method = RequestMethod.GET)
    @ResponseBody
    public RestResult<TreeNodeVO> getFileTree(@RequestHeader("token") String token){
        RestResult<TreeNodeVO> result = new RestResult<TreeNodeVO>();
        UserFile userFile = new UserFile();
        User sessionUser = userService.getUserByToken(token);
        userFile.setUserId(sessionUser.getUserId());

        List<UserFile> filePathList = userfileService.selectFilePathTreeByUserId(sessionUser.getUserId());
        TreeNodeVO resultTreeNode = new TreeNodeVO();
        resultTreeNode.setLabel("/");

        for (int i = 0; i < filePathList.size(); i++){
            String filePath = filePathList.get(i).getFilePath() + filePathList.get(i).getFileName() + "/";

            Queue<String> queue = new LinkedList<>();

            String[] strArr = filePath.split("/");
            for (int j = 0; j < strArr.length; j++){
                if (!"".equals(strArr[j]) && strArr[j] != null){
                    queue.add(strArr[j]);
                }

            }
            if (queue.size() == 0){
                continue;
            }
            resultTreeNode = insertTreeNode(resultTreeNode,"/", queue);


        }
        result.setSuccess(true);
        result.setData(resultTreeNode);         //携带数据是文件树的根结点
        return result;
    }

    private TreeNodeVO insertTreeNode(TreeNodeVO treeNode, String filePath, Queue<String> nodeNameQueue) {
        List<TreeNodeVO> childrenTreeNodes = treeNode.getChildren();
        String currentNodeName = nodeNameQueue.peek();
        if (currentNodeName == null){
            return treeNode;
        }
        Map<String, String> map = new HashMap<>();
        filePath = filePath + currentNodeName + "/";
        map.put("filePath", filePath);

        if (!isExistPath(childrenTreeNodes, currentNodeName)){  //1、判断有没有该子节点，如果没有则插入
            //插入
            TreeNodeVO resultTreeNode = new TreeNodeVO();
            resultTreeNode.setAttributes(map);
            resultTreeNode.setLabel(nodeNameQueue.poll());
            // resultTreeNode.setId(treeid++);
            childrenTreeNodes.add(resultTreeNode);
        }else{  //2、如果有，则跳过
            nodeNameQueue.poll();
        }

        if (nodeNameQueue.size() != 0) {
            for (int i = 0; i < childrenTreeNodes.size(); i++) {
                TreeNodeVO childrenTreeNode = childrenTreeNodes.get(i);
                if (currentNodeName.equals(childrenTreeNode.getLabel())){
                    childrenTreeNode = insertTreeNode(childrenTreeNode, filePath, nodeNameQueue);
                    childrenTreeNodes.remove(i);
                    childrenTreeNodes.add(childrenTreeNode);
                    treeNode.setChildren(childrenTreeNodes);
                }
            }
        }else{
            treeNode.setChildren(childrenTreeNodes);
        }
        return treeNode;
    }

    private boolean isExistPath(List<TreeNodeVO> childrenTreeNodes, String path) {
        boolean isExistPath = false;
        try {
            for (int i = 0; i < childrenTreeNodes.size(); i++){
                if (path.equals(childrenTreeNodes.get(i).getLabel())){
                    isExistPath = true;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return isExistPath;
    }

    @Operation(summary = "文件移动", description = "可以移动文件或者目录", tags = { "file" })
    @RequestMapping(value = "/movefile", method = RequestMethod.POST)
    @ResponseBody
    public RestResult<String> moveFile(@RequestBody MoveFileDTO moveFileDto, @RequestHeader("token") String token) {
        User sessionUser = userService.getUserByToken(token);
        String oldfilePath = moveFileDto.getOldFilePath();
        String newfilePath = moveFileDto.getFilePath();
        String fileName = moveFileDto.getFileName();
        String extendName = moveFileDto.getExtendName();

        userfileService.updateFilepathByFilepath(oldfilePath, newfilePath, fileName, extendName, sessionUser.getUserId());
        return RestResult.success();

    }

    @Operation(summary = "批量移动文件", description = "可以同时选择移动多个文件或者目录", tags = { "file" })
    @RequestMapping(value = "/batchmovefile", method = RequestMethod.POST)
    @ResponseBody
    public RestResult<String> batchMoveFile(@RequestBody BatchMoveFileDTO batchMoveFileDto,
                                            @RequestHeader("token") String token) {

        User sessionUser = userService.getUserByToken(token);
        String files = batchMoveFileDto.getFiles();
        String newfilePath = batchMoveFileDto.getFilePath();
        List<UserFile> userFiles = JSON.parseArray(files, UserFile.class);

        for (UserFile userFile : userFiles) {
            userfileService.updateFilepathByFilepath(userFile.getFilePath(), newfilePath, userFile.getFileName(),
                    userFile.getExtendName(), sessionUser.getUserId());
        }

        return RestResult.success().data("批量移动文件成功");

    }

    @Operation(summary = "文件重命名", description = "文件重命名", tags = {"file"})
    @RequestMapping(value = "/renamefile", method = RequestMethod.POST)
    @ResponseBody
    public RestResult<String> renameFile(@RequestBody RenameFileDTO renameFileDto, @RequestHeader("token") String token) {
        User sessionUser = userService.getUserByToken(token);
        UserFile userFile = userfileService.getById(renameFileDto.getUserFileId());
        List<UserFile> userFiles =
                userfileService.selectUserFileByNameAndPath(renameFileDto.getFileName(), userFile.getFilePath(), sessionUser.getUserId());
        if (userFiles!=null && !userFiles.isEmpty()){
            //根据新文件名和文件路径找到了文件，证明这个名字已经被用了
            return RestResult.fail().message("同名文件已经存在");
        }
        if (1==userFile.getIsDir()){
            //这是一个文件夹
            LambdaUpdateWrapper<UserFile> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.set(UserFile::getFileName,renameFileDto.getFileName())
                    .set(UserFile::getUploadTime,DateUtil.getCurrentTime())
                    .eq(UserFile::getUserFileId,renameFileDto.getUserFileId());
            userfileService.update(lambdaUpdateWrapper);
            userfileService.replaceUserFilePath(userFile.getFilePath()+renameFileDto.getFileName()+"/",
                    userFile.getFilePath()+userFile.getFileName()+"/", sessionUser.getUserId());
        }else {
            //这不是一个文件夹，是一个文件
            File file = fileService.getById(userFile.getFileId());      //这里获取到File对象并没有作用
            /**
             * 获取LambdaUpdateWrapper对象，传入UserFile对象
             * 调用该对象的eq方法锁定文件，通过DTO中的userfileid
             * 然后对该文件进行改名，修改文件上传时间为当前时间
             */
            LambdaUpdateWrapper<UserFile> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.set(UserFile::getFileName,renameFileDto.getFileName())
                    .set(UserFile::getUploadTime,DateUtil.getCurrentTime())
                    .eq(UserFile::getUserFileId,renameFileDto.getUserFileId());

            userfileService.update(lambdaUpdateWrapper);        //对于一个文件，直接更新改名就行
        }
        return RestResult.success();
    }




}
