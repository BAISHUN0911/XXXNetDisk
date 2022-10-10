package com.lanqiao.netdisk.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 测试Controller
 */

@Tag(name = "mytest",description = "该接口为测试接口，用于开发过程中测试")
@RestController
@RequestMapping("/mytest")
public class TestController {


    @Operation(summary = "接口测试",
            description = "用于测试@RequestHeader注解，获取客户端HTTP请求的Header",
            tags = {"mytest"}
    )
    @PostMapping("/getHeader")
    @ResponseBody
    public String getHeader(@RequestHeader("host") String host,
                            @RequestHeader(name = "my-header",required = false,defaultValue = "3") String myHeader){
        System.out.println("host:"+host);
        System.out.println("accept:"+myHeader);
        return "success"+myHeader;
    }



}
