package com.own.send.server.controller;

import com.own.face.util.base.BaseController;
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
public class EmailController extends BaseController {

    @Autowired
    private EmailSvc emailSvc;
    @Autowired
    private CommonInfoSvc cifSvc;
    @Autowired
    public ConfigProperty configProperty;

    @ApiOperation(value = "根据id查询email信息") @GetMapping("/email/{id}")
    public @ResponseBody Email getEmail(@PathVariable int id){
        log.info("Get Email info !");
        Email email = null;
        email = emailSvc.findEmailById(id);
        return email;
    }

    @ApiOperation(value = "查询邮件列表")
   @GetMapping("/email")
    public @ResponseBody List<Email> getInfo(@RequestParam Integer id, String title, String content){
        log.info("Get Emails info !");
        log.info("获取短信列表信息.....id:"+id+" title:"+title+"  content:"+content);
        StringBuffer sb = new StringBuffer();
        if(id!=null && !"".equals(id)){
            sb.append(" or e.id="+id);
        }
        if(null!=title && !"".equals(title)){
            sb.append(" or e.title like '%"+title+"%'");//like语法，注意单引号
        }

        if(null!=content && !"".equals(content)){
            sb.append(" or e.content like '%"+content+"%'");
        }
        List list = emailSvc.getEmails(sb.toString());
        log.info("查询集合长度："+list.size());

        return list;
    }

    @ApiOperation(value = "发送邮件")
    @PostMapping("/email")
    public String sendEmail(@RequestBody Map param) throws Throwable{//use this method to get param value
        Email mail = (Email)param.get("Email");
       log.info("传入email:{}",mail.getTitle());
        String result = "";
        String emailAccount = param.get("emailAccount")!=null?param.get("emailAccount").toString():null;//email 账号
        String title =  param.get("title")!=null?param.get("title").toString():null;
        String content =  param.get("content")!=null?param.get("content").toString():null;
        log.info("邮箱账号："+emailAccount+" 邮件标题："+title+" 邮件内容："+content);
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
        return result;
    }


    @ApiOperation("修改邮件，比如暂存草稿")
    @PutMapping("/email")
    public Email updateSms(@RequestBody Email email){//note:客户端是用requestbody提交的，这里如果写成requestX别的东西，会找不到这个方法
        Email ss = email;
        log.info("修改短信信息......"+ss.getContent());
        if(ss!=null){
            try{
                emailSvc.save(ss);//保存或修改
                //更改公共信息表
            }catch(Exception ee){
                log.error("修改短信失败,原因："+ee.getMessage());
                ee.printStackTrace();
            }
        }
        return ss;
    }

    @ApiOperation(value = "mvc 测试")
    @GetMapping("/email/test")
    public ModelAndView getEmailTest(){
        ModelAndView mv = new ModelAndView("index");
        System.out.println("Internet the index page !");
        return mv;
    }

}
