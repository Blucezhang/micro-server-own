package com.own.send.server.controller;

import com.own.face.util.Resp;
import com.own.send.server.domain.CommonInfo;
import com.own.send.server.domain.Sms;
import com.own.send.server.prop.ConfigProperty;
import com.own.send.server.service.CommonInfoSvc;
import com.own.send.server.service.SmsSvc;
import com.own.send.server.util.SendSms;
import com.own.send.server.util.sms.MsgResult;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Bluce on 2018/4/4.
 */
@Slf4j
@RestController
@RequestMapping(value = "/Info")
public class SmsController {

    @Autowired
    private SmsSvc smsSvc;
    @Autowired
    private CommonInfoSvc cifSvc;
    @Autowired
    public ConfigProperty configProperty;

    @Operation(summary = "查询短信列表（搜索功能，无搜索条件时，查所有）")
    @GetMapping("/sms")
    public Resp getMessageList(@RequestParam(required = false) Integer id,
                               @RequestParam(required = false) String title,
                               @RequestParam(required = false) String content) {
        log.info("查询短信列表，筛选字段：id={}, titlePresent={}, contentPresent={}", id,
                title != null && !title.trim().isEmpty(), content != null && !content.trim().isEmpty());
        List list = smsSvc.getSms(id, title, content);
        log.info("查询集合长度：" + list.size());
        return new Resp(list);

    }

    /**
     * 发送短信,post
     *
     * @return
     */
    @Operation(summary = "发送短信")
   @PostMapping("/sms")
    public Resp sendSms(@RequestBody Map param) throws Throwable {//use this method to get param value
        String Action = param.get("Action") != null ? param.get("Action").toString() : null;
        String mobile = param.get("mobile") != null ? param.get("mobile").toString() : null;
        String content = param.get("content") != null ? param.get("content").toString() : null;
        String result = "";

        SendSms sendsms = new SendSms();
        if (sendsms.isMobile(mobile)) {//校验手机格式

            String serviceURL = configProperty.getServiceURL();
            String sn = configProperty.getSn();
            String pwd = configProperty.getPwd();


            if (null != Action && !"".equals(Action)) {
                if ("send".equals(Action)) {
                    log.info("发送信息.....");
                    Sms msg = new Sms();
                    log.info("发送短信请求已通过基础格式校验");
                    if (mobile != null && content != null) {

                        //短信发送测试成功，暂时注释掉
                        MsgResult msgresult = sendsms.sendSms(mobile, content, serviceURL, sn, pwd);

                        //返回结果：errMsg:null,sysSuccMsg:null,state:succ
                       log.info("失败信息：" + msgresult.getErrMsg() + " 成功信息： " + msgresult.getSysSuccMsg() + " state:" + msgresult.getState());
                        //将信息入库
                        msg.setReceiveMobile(mobile);
                        msg.setContent(content);
                        msg.setCreateTime(new Date());
                        msg.setThirdResult(msgresult.getState());
                        msg.setEndTime(new Date());
                        msg.setSendXML("xml");
                        msg.setReceiveXML("xml");
                        msg.setMsgType(0);//发件
                        msg.setDraft(0);//是否草稿，1否
                        msg.setFlag(0);//是否删除，0否

                        msg = smsSvc.save(msg);
                        if (msg != null && msg.getId() != null && 0 < msg.getId()) {
                            log.info("保存成功,返回id：" + msg.getId());//此时会生成新的id并返回
                            //此时要保存公共信息表
                            CommonInfo cif = new CommonInfo();
                            cif.setReceiveAccount(mobile);
                            cif.setContent(content);
                            cif.setInfoId(msg.getId());
                            cif.setInfoType("SMS");//短信
                            cif.setType(0);//发件
                            cif.setSendTime(new Date());

                            cifSvc.save(cif);
                            if (cif != null && cif.getId() != null && 0 < cif.getId()) {
                                log.info("保存公共信息表成功,id:" + cif.getId());
                            } else {
                                log.error("保存公共信息表失败!");
                            }
                            result = "success";
                        } else {
                            result = "failed";
                            log.info("保存失败");
                        }

                    } else {
                        log.warn("短信发送请求缺少必要内容");
                        result = "failed";
                    }
                } else if ("receive".equals(Action)) {
                    log.info("接收信息.....");
                    Sms msg = new Sms();
                  log.info("接收短信记录请求已通过手机号格式校验");
                    if (mobile != null && content != null) {
                        //将信息入库
                        msg.setSendMobile(mobile);
                        msg.setContent(content);
                        msg.setCreateTime(new Date());
                        msg.setEndTime(new Date());
                        msg.setSendXML("xml");
                        msg.setReceiveXML("xml");
                        msg.setMsgType(1);//发件
                        msg.setDraft(0);//是否草稿，1否
                        msg.setFlag(0);//是否删除，0否
                        msg = smsSvc.save(msg);
                        if (msg != null && msg.getId() != null && 0 < msg.getId()) {
                            log.info("保存成功,返回id：" + msg.getId());//此时会生成新的id并返回
                            CommonInfo cif = new CommonInfo();
                            cif.setSendAccount(mobile);
                            cif.setContent(content);
                            cif.setInfoId(msg.getId());
                            cif.setInfoType("SMS");//短信
                            cif.setType(1);//发件
                            cif.setReceiveTime(new Date());

                            cifSvc.save(cif);
                            if (cif != null && cif.getId() != null && 0 < cif.getId()) {
                                log.info("保存公共信息表成功,id:" + cif.getId());
                            } else {
                                log.error("保存公共信息表失败!");
                            }
                            result = "success";
                        } else {
                            result = "failed";
                            log.info("保存失败");
                        }
                    } else {
                        log.warn("短信接收记录请求缺少必要内容");
                        result = "failed";
                    }
                }
            }
        } else {
            log.error("手机号格式错误");
            result = "failed";
        }
        return new Resp(result);
    }

    /**
     * 根据id查询短信信息
     *
     * @param id
     * @return
     */
   @GetMapping("/sms/{id}")
    public Resp findSms(@PathVariable Integer id) {
        log.info("根据id查询短信信息");
        return new Resp(smsSvc.findSmsById(id));
    }

    @Operation(summary = "修改短信，比如暂存草稿,入参为Sms实体对象")
    @PutMapping("/sms")
    public Resp updateSms(@RequestBody Sms sms) {
        Sms ss = sms;
        if (ss != null) {
            log.info("修改短信信息，id={}, contentPresent={}", ss.getId(),
                    ss.getContent() != null && !ss.getContent().isEmpty());
            try {
                smsSvc.updateSms(ss);
                //更改公共信息表
            } catch (Exception ee) {
                log.error("修改短信失败", ee);
            }
        }
        return new Resp(sms);
    }

}
