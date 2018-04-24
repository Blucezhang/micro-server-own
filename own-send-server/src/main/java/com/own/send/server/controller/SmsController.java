package com.own.send.server.controller;

import com.own.send.server.domain.CommonInfo;
import com.own.send.server.domain.Sms;
import com.own.send.server.service.CommonInfoSvc;
import com.own.send.server.service.SmsSvc;
import com.own.send.server.util.SendSms;
import com.siaya.action.core.MsgResult;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Bluce on 2018/4/4.
 */
@RestController
@RequestMapping(value = "/Info")
@Slf4j
public class SmsController {

    @Autowired
    private SmsSvc smsSvc;
    @Autowired
    private CommonInfoSvc cifSvc;
    @Autowired
    public ConfigProperty configProperty;

    @ApiOperation(value = "查询短信列表（搜索功能，无搜索条件时，查所有）")
    @GetMapping("/sms")
    public List getMessageList(@RequestParam Integer id, String title, String content) {
        log.info("获取短信列表信息.....id:" + id + " title:" + title + "  content:" + content);
        StringBuffer sb = new StringBuffer();
        if (id != null && !"".equals(id)) {
            sb.append(" or s.id=" + id);
        }
        if (null != title && !"".equals(title)) {
            sb.append(" or s.title like '%" + title + "%'");//like语法，注意单引号
        }

        if (null != content && !"".equals(content)) {
            sb.append(" or s.content like '%" + content + "%'");
        }

        List list = smsSvc.getSms(sb.toString());
        log.info("查询集合长度：" + list.size());
        return list;

    }

    /**
     * 发送短信,post
     *
     * @return
     */
    @ApiOperation(value = "发送短信")
   @PostMapping("/sms")
    public String sendSms(@RequestBody Map param) throws Throwable {//use this method to get param value

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
                    Properties props = PropertiesLoaderUtils.loadAllProperties("Properties/Action.properties");

                    Sms msg = new Sms();
                    System.out.println("手机号：" + mobile + " 短信内容：" + content + " 发送短信url：" + props.getProperty("sms.serviceURL"));
                    if (mobile != null && content != null) {

                        //短信发送测试成功，暂时注释掉
                        MsgResult msgresult = sendsms.sendSms(mobile, content, serviceURL, sn, pwd);

                        //返回结果：errMsg:null,sysSuccMsg:null,state:succ
                        System.out.println("失败信息：" + msgresult.getErrMsg() + " 成功信息： " + msgresult.getSysSuccMsg() + " state:" + msgresult.getState());
                        //将信息入库
                        msg.setReceiveMobile(mobile);
                        msg.setContent(content);
                        msg.setCreateTime(new Date());
                        msg.setThirdResult(msgresult.getState());
//						msg.setThirdResult("succ");
                        msg.setEndTime(new Date());
                        msg.setSendXML("xml");
                        msg.setReceiveXML("xml");
                        msg.setMsgType(0);//发件
                        msg.setDraft(0);//是否草稿，1否
                        msg.setFlag(0);//是否删除，0否

                        msg = smsSvc.save(msg);
                        if (null != msg.getId() && 0 < msg.getId()) {
                            log.info("保存成功,返回id：" + msg.getId());//此时会生成新的id并返回
//							result = result.getState();
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
                        System.out.println("input params is null ！");
                        result = "failed";
                    }
                } else if ("receive".equals(Action)) {
                    log.info("接收信息.....");
                    Sms msg = new Sms();
                    System.out.println("手机号：" + mobile + " 短信内容：" + content);
                    if (mobile != null && content != null) {
                        //将信息入库
                        msg.setSendMobile(mobile);
                        msg.setContent(content);
                        msg.setCreateTime(new Date());
//						msg.setThirdResult(result.getState());
//						msg.setThirdResult("succ");
                        msg.setEndTime(new Date());
                        msg.setSendXML("xml");
                        msg.setReceiveXML("xml");
                        msg.setMsgType(1);//发件
                        msg.setDraft(0);//是否草稿，1否
                        msg.setFlag(0);//是否删除，0否
                        msg = smsSvc.save(msg);
                        if (null != msg.getId() && 0 < msg.getId()) {
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
                        System.out.println("input params is null ！");
                        result = "failed";
                    }
                }
            }
        } else {
            log.error("手机号格式错误");
            result = "failed";
        }
        return result;
    }

    /**
     * 根据id查询短信信息
     *
     * @param id
     * @return
     */
   @GetMapping("/sms/{id}")
    public Sms findSms(@PathVariable Integer id) {
        Sms sms = smsSvc.findSmsById(id);
        return sms;
    }

    @ApiOperation(value = "修改短信，比如暂存草稿,入参为Sms实体对象")
    @PutMapping("/sms")
    public Sms updateSms(@RequestBody Sms sms) {
        Sms ss = sms;
        log.info("修改短信信息......" + ss.getContent());
        if (ss != null) {
            try {
                smsSvc.updateSms(ss);
                //更改公共信息表
            } catch (Exception ee) {
                log.error("修改短信失败,原因：" + ee.getMessage());
                ee.printStackTrace();
            }
        }
        return sms;
    }


}
