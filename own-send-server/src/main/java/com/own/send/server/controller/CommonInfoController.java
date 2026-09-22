package com.own.send.server.controller;

import com.own.face.util.Resp;
import com.own.send.server.domain.CommonInfo;
import com.own.send.server.service.CommonInfoSvc;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by Bluce on 2018/4/4.
 */
@RestController
@RequestMapping(value = "/Info")
@Slf4j
public class CommonInfoController{

    @Autowired
    private CommonInfoSvc cifSvc;
    /**
     * ,使用post
     * @return
     */
    @Operation(summary = "查询公共信息列表")
    @GetMapping
    public Resp findCommonInfo(@RequestParam(required = false) Integer id,
                               @RequestParam(required = false) String title,
                               @RequestParam(required = false) String content,
                               @RequestParam(required = false) String sendAccount,
                               @RequestParam(required = false) String receiveAccount) {
        log.info("查询公共信息列表");
        log.info("查询公共信息，筛选字段：id={}, titlePresent={}, contentPresent={}, sendAccountPresent={}, receiveAccountPresent={}",
                id, title != null && !title.trim().isEmpty(), content != null && !content.trim().isEmpty(),
                sendAccount != null && !sendAccount.trim().isEmpty(), receiveAccount != null && !receiveAccount.trim().isEmpty());
        List cilist = cifSvc.findList(id, title, content, sendAccount, receiveAccount);
        log.info("获取公共信息集合长度：" + cilist.size());
        return new Resp(cilist);
    }
}
