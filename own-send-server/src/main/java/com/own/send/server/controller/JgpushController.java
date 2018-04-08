package com.own.send.server.controller;

import com.own.send.server.domain.Jgpush;
import com.own.send.server.service.JgpushSvc;
import com.own.send.server.util.SendJgpush;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * Created by Bluce on 2018/4/4.
 */
@RestController
@RequestMapping(value = "/Info")
@Slf4j
public class JgpushController {

    @Autowired
    private JgpushSvc jgpushSvc;
    @Autowired
    public ConfigProperty configProperty;

    /**
     * 根据id查询极光信息
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/jgpush/{id}", method = RequestMethod.GET)
    public Jgpush findJgpush(@PathVariable Integer id) {
        return jgpushSvc.findJgpushById(id);
    }

    /**
     * 推送极光消息
     *
     * @param jp
     * @return
     */
    @RequestMapping(value = "/jgpush", method = RequestMethod.POST)
    public String sendJgpush(@RequestBody Jgpush jp) {

        //获取配置信息
        String appKey = configProperty.getAppKey();
        String masterSecret = configProperty.getMasterSecret();
        log.info("获取配置信息appkey：" + appKey + " masterSecret:" + masterSecret);

        String result = "";
        SendJgpush sjp = new SendJgpush();
        //1成功2失败
        result = sjp.sendJpush(jp.getTitle(), jp.getContent(), jp.getPlatform().toString(), jp.getSendId(), jp.getReleaseFun(), appKey, masterSecret);
        log.info("极光推送结果：" + result);
        //保存
        Jgpush jgp = new Jgpush();
        jgp.setTitle(jp.getTitle());
        jgp.setContent(jp.getContent());
        jgp.setPlatform(jp.getPlatform());
        jgp.setSendId(jp.getSendId());
        jgp.setReleaseFun(jp.getReleaseFun());
        jgp.setCreateTime(new Date());
        jgp.setSendStatus(Integer.valueOf(result));
        jgp.setJpushType(0);//收件
        if (result != null && result.equals("1")) {
            jgp.setSendResult("推送成功");
        } else if (result != null && result.equals("2")) {
            jgp.setSendResult("推送失败");
        } else {
            jgp.setSendResult("推送失败");
        }
        jgp.setDrapt(0);//否
        jgp.setFlag(0);
        jgp.setEndTime(new Date());

        return result;
    }

    /**
     * 搜索信息列表
     *
     * @param title
     * @return
     */
    @RequestMapping(value = "/jgpush", method = RequestMethod.GET)
    public List findJgpushs(@RequestParam String title, String content) {
        String wheresql = " or ";
        if (null != title && !"".equals(title)) {
            wheresql += " jp.title like '%" + title + "%' ";
        }

        if (null != content && !"".equals(content)) {
            wheresql += " or jp.content like '%" + content + "%' ";
        }

        List jps = jgpushSvc.findJgpushs(wheresql);
        return jps;
    }

}
