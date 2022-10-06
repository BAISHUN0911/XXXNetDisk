package com.lanqiao.netdisk.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lanqiao.netdisk.common.RestResult;
import com.lanqiao.netdisk.dto.CreateFileDTO;
import com.lanqiao.netdisk.dto.UserfileListDTO;
import com.lanqiao.netdisk.model.User;
import com.lanqiao.netdisk.model.UserFile;
import com.lanqiao.netdisk.service.FileService;
import com.lanqiao.netdisk.service.UserFileService;
import com.lanqiao.netdisk.service.UserService;
import com.lanqiao.netdisk.util.DateUtil;
import com.lanqiao.netdisk.vo.UserfileListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        userFile.setIsDir(1);//创建的是文件夹
        userFile.setUploadTime(DateUtil.getCurrentTime());
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
        System.out.println("fileList======================"+fileList);
        LambdaQueryWrapper<UserFile> userFileLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userFileLambdaQueryWrapper.eq(UserFile::getUserId,
                sessionUser.getUserId()).eq(UserFile::getFilePath,userfileListDTO.getFilePath());
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

}
