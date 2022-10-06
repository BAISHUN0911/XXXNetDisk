package com.lanqiao.netdisk.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanqiao.netdisk.mapper.FileMapper;
import com.lanqiao.netdisk.model.File;
import com.lanqiao.netdisk.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {

}
