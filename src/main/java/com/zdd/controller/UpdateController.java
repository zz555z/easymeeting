package com.zdd.controller;

import com.zdd.aop.annotation.GlobalInterceptor;
import com.zdd.entry.vo.ResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/update")
@Slf4j
public class UpdateController {

    @PostMapping("/checkVersion")
    @GlobalInterceptor()
    public ResponseVO checkVersion() {
        return ResponseVO.success();
    }
}
