package com.lanqiao.netdisk.controller;

import com.lanqiao.netdisk.common.RestResult;
import com.lanqiao.netdisk.dto.DownloadFileDTO;
import com.lanqiao.netdisk.dto.UploadFileDTO;
import com.lanqiao.netdisk.model.File;
import com.lanqiao.netdisk.model.User;
import com.lanqiao.netdisk.model.UserFile;
import com.lanqiao.netdisk.service.FileService;
import com.lanqiao.netdisk.service.FiletransferService;
import com.lanqiao.netdisk.service.UserFileService;
import com.lanqiao.netdisk.service.UserService;
import com.lanqiao.netdisk.util.DateUtil;
import com.lanqiao.netdisk.util.FileUtil;
import com.lanqiao.netdisk.vo.UploadFileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;

/**
 * 文件传输类接口
 * 作为文件传输接口，主要职责为文件上传，下载及删除
 */

@Tag(name = "filetransfer",description = "该接口为文件传输接口，主要用来做文件的上传和下载")
@RestController
@RequestMapping("/filetransfer")
public class FiletransferController {

    @Resource
    UserService userService;
    @Resource
    FileService fileService;
    @Resource
    UserFileService userFileService;
    @Resource
    FiletransferService filetransferService;

    @Operation(summary = "极速上传",
            description = "校验文件MD5判断文件是否存在，如果存在直接上传成功并返回skipUpload=true，如果不存在返回skipUpload=false需要再次调用该接口的POST方法",
            tags = {"filetransfer"}
    )
    @GetMapping(value="/uploadfile")
    @ResponseBody
    public RestResult<UploadFileVO> uploadFileSpeed(UploadFileDTO uploadFileDto, @RequestHeader("token") String token) {
        User sessionUser = userService.getUserByToken(token);   //通过客户端传来的token值认证身份，识别用户
        if (sessionUser == null){
            return RestResult.fail().message("未登录,请先登录");
        }
        UploadFileVO uploadFileVO = new UploadFileVO();
        HashMap<String, Object> param = new HashMap<>();
        param.put("identifier",uploadFileDto.getIdentifier());  //文件的md5码由前端生成，传给服务器
        synchronized (FiletransferController.class){            //添加同步锁
            List<File> list = fileService.listByMap(param);     //mybatisplus提供Service方法，这里通过文件的md5查询文件
            if (list != null && !list.isEmpty()){
                File file = list.get(0);    //获取这个文件
                UserFile userFile = new UserFile();                 //注意数据库中的userfile表，明白这个表的含义。为了避免使用外键，使用userfile表关联user和file表
                userFile.setFileId(file.getFileId());               //设置文件id
                userFile.setUserId(sessionUser.getUserId());        //设置用户id，通过token确定一名用户，获得用户信息
                userFile.setFilePath(uploadFileDto.getFilePath());  //设置文件路径，通过接口参数DTO获取
                String filename = uploadFileDto.getFilename();      //获取完整文件名，例如jingjicang.mp3，也是通过接口参数DTO获取
                userFile.setFileName(filename.substring(0,filename.lastIndexOf(".")));  //设置文件名，jingjicang
                userFile.setExtendName(FileUtil.getFileExtendName(filename));               //设置文件扩展名，通过自写FileUtil
                userFile.setIsDir(0);                                   //设置该文件是非目录的
                userFile.setUploadTime(DateUtil.getCurrentTime());      //设置文件上传时间
                userFile.setDeleteFlag(0);
                userFileService.save(userFile);                         //将这个文件保存到userfile表中
                // fileService.increaseFilePointCount(file.getFileId());
                uploadFileVO.setSkipUpload(true);                       //这个文件成功使用了秒传，跳过了常规上传步骤
            }else {
                uploadFileVO.setSkipUpload(false);      //如果查询出的文件集合为空，证明这个文件没上传过，不能文件秒传，只能通过普通方式上传。
            }
        }
        return RestResult.success().data(uploadFileVO);
    }

    @Operation(summary = "上传文件", description = "真正的上传文件接口", tags = {"filetransfer"})
    @RequestMapping(value = "/uploadfile", method = RequestMethod.POST)
    @ResponseBody
    public RestResult<UploadFileVO> uploadFile(HttpServletRequest request, UploadFileDTO uploadFileDto, @RequestHeader("token") String token) {

        User sessionUser = userService.getUserByToken(token);
        if (sessionUser == null){
            return RestResult.fail().message("未登录,请先登录");
        }

        filetransferService.uploadFile(request, uploadFileDto, sessionUser.getUserId());
        UploadFileVO uploadFileVo = new UploadFileVO();
        return RestResult.success().data(uploadFileVo);

    }

    @Operation(summary = "下载文件", description = "下载文件接口", tags = {"filetransfer"})
    @RequestMapping(value = "/downloadfile", method = RequestMethod.GET)
    public void downloadFile(HttpServletResponse response, DownloadFileDTO downloadFileDTO) {
        filetransferService.downloadFile(response, downloadFileDTO);
    }



}
