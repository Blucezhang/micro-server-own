package com.own.send.server.controller;

import com.own.face.util.Resp;
import com.own.send.server.domain.CommonInfo;
import com.own.send.server.domain.Email;
import com.own.send.server.prop.ConfigProperty;
import com.own.send.server.service.CommonInfoSvc;
import com.own.send.server.service.EmailSvc;
import com.own.send.server.util.SendMail;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Bluce on 2018/4/4.
 */
@Slf4j
@RestController
@RequestMapping(value="/Info")
public class EmailController {

    @Autowired
    private EmailSvc emailSvc;
    @Autowired
    private CommonInfoSvc cifSvc;
    @Autowired
    public ConfigProperty configProperty;

    @ApiOperation(value = "根据id查询email信息") @GetMapping("/email/{id}")
    public @ResponseBody Resp getEmail(@PathVariable int id){
        log.info("Get Email info !");
        Email email = null;
        email = emailSvc.findEmailById(id);
        return new Resp(email);
    }

    @ApiOperation(value = "查询邮件列表")
   @GetMapping("/email")
    public @ResponseBody Resp getInfo(@RequestParam(required = false) Integer id,
                                      @RequestParam(required = false) String title,
                                      @RequestParam(required = false) String content){
        log.info("Get Emails info !");
        log.info("查询邮件列表，筛选字段：id={}, titlePresent={}, contentPresent={}", id,
                title != null && !title.trim().isEmpty(), content != null && !content.trim().isEmpty());
        List list = emailSvc.getEmails(id, title, content);
        log.info("查询集合长度："+list.size());
        return new Resp(list);
    }

    @ApiOperation(value = "发送邮件")
    @PostMapping("/email")
    public Resp sendEmail(@RequestBody Map param) throws Throwable{//use this method to get param value
        String result = "";
        String emailAccount = param.get("emailAccount")!=null?param.get("emailAccount").toString():null;//email 账号
        String title =  param.get("title")!=null?param.get("title").toString():null;
        String content =  param.get("content")!=null?param.get("content").toString():null;
        log.info("收到邮件发送请求，recipientPresent={}, titlePresent={}, contentPresent={}",
                emailAccount != null, title != null, content != null);
        if(null!=emailAccount && null!=title && null!=content){
            Email email = new Email();
            SendMail sm = new SendMail();


            String iocMailSmtpHost=configProperty.getHost();//SMTP服务器
            String iocMailSmtpAuth=configProperty.getAuth();//SMTP是否需要验证
            String iocSmtp="smtp";
            String iocMailFrom=configProperty.getFrom();//发件人
            String iocMailSubject=configProperty.getSubject();//邮件主题
            String iocMailFromUser=configProperty.getUser();//发件人用户名
            String iocMailFromPassWord=configProperty.getPassword();//发件人密码


            sm.sendMail(emailAccount, title, content,iocMailSmtpHost,iocMailSmtpAuth,iocSmtp,iocMailFrom,iocMailFromUser,iocMailFromPassWord);//发送邮件

            email.setTitle(title);
            email.setContent(content);
            email.setReceiveEmail(emailAccount);
            email.setCreateTime(new Date());
            email.setEndTime(new Date());
            email.setSendTime(new Date());
            email.setEmailType(0);//发件
            email.setDraft(0);//非草稿
            emailSvc.save(email);

            if(null!=email.getId()){
                log.info("保存邮件成功");
                CommonInfo cif = new CommonInfo();
                cif.setReceiveAccount(emailAccount);
                cif.setContent(content);
                cif.setInfoId(email.getId());
                cif.setInfoType("EMAIL");//短信
                cif.setType(0);//发件
                cif.setSendTime(new Date());

                cifSvc.save(cif);
                if(cif!=null && cif.getId()!=null && 0<cif.getId()){
                    log.info("保存公共信息表成功,id:"+cif.getId());
                }else{
                    log.error("保存公共信息表失败!");
                }
                result = "success";
            }else{
                result = "failed";
                log.info("保存邮件成功");
            }
        }
        return new Resp(result);
    }


    @ApiOperation("修改邮件，比如暂存草稿")
    @PutMapping("/email")
    public Resp updateSms(@RequestBody Email email){//note:客户端是用requestbody提交的，这里如果写成requestX别的东西，会找不到这个方法
        Email ss = email;
        if(ss!=null){
            log.info("修改邮件信息，id={}, contentPresent={}", ss.getId(),
                    ss.getContent() != null && !ss.getContent().isEmpty());
            try{
                emailSvc.save(ss);//保存或修改
                //更改公共信息表
            }catch(Exception ee){
                log.error("修改短信失败,原因："+ee.getMessage());
                log.error("修改邮件失败", ee);
            }
        }
        return new Resp(ss);
    }

}
