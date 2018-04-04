package com.own.send.server.controller;

import com.own.send.server.domain.CommonInfo;
import com.own.send.server.service.CommonInfoSvc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by Bluce on 2018/4/4.
 */
@RestController
@RequestMapping(value = "/Info")
@Slf4j
public class CommonInfoController {

    @Autowired
    private CommonInfoSvc cifSvc;

    /**
     * 查询公共信息列表,使用post
     *
     * @return
     */
    @RequestMapping(method = {RequestMethod.GET})
    public List<CommonInfo> findCommonInfo(@RequestParam Integer id, String title, String content, String sendAccount, String receiveAccount) {
        id = id != null ? id : null;
        title = title != null ? title : null;
        content = content != null ? content : null;
        sendAccount = sendAccount != null ? sendAccount : null;
        receiveAccount = receiveAccount != null ? receiveAccount : null;

        log.info("查询公共信息列表");
        StringBuffer wheresql = new StringBuffer();

        log.info("获取参数信息：id:" + id + " title:" + title + "  content:" + content + " sendAccount:" + sendAccount + "  receiveAccount:" + receiveAccount);
        if (null != id) {
            wheresql.append(" or c.infoId=" + id);
        }
        if (null != title && !"".equals(title)) {
            wheresql.append(" or c.title like'%" + title + "%'");
        }
        if (null != content && !"".equals(content)) {
            wheresql.append(" or c.content like '%" + content + "%'");
        }
        if (null != sendAccount && !"".equals(sendAccount)) {
            wheresql.append(" or c.sendAccount like '%" + sendAccount + "%'");
        }
        if (null != receiveAccount && !"".equals(receiveAccount)) {
            wheresql.append(" or c.receiveAccount like '%" + receiveAccount + "%'");
        }

        List cilist = cifSvc.findList(wheresql.toString());
        log.info("获取公共信息集合长度：" + cilist.size());
        return cilist;
    }
}
