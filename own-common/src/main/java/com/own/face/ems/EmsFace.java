package com.own.face.ems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.own.face.core.FaceBase;
import com.own.face.core.IfException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class EmsFace extends FaceBase {

    protected String serviceUrl = "http://EMSERVER/";
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取邮件信息
     *
     * @param id
     * @return
     * @throws IfException
     */
    public Email getEmailById(int id) throws IfException {
        Email email = get(serviceUrl + "/Info/email/{id}", Email.class, id);
        return email;
    }
    /**
     * 发送短信
     *
     * @param mobile
     * @param content
     * @return
     * @throws IfException
     */
    public boolean sendMsg(String mobile, String content) throws IfException {
        try {
            Map<String, Object> requestBody = new HashMap<String, Object>();
            requestBody.put("Action", "send");
            requestBody.put("mobile", mobile);
            requestBody.put("content", content);
            String responseText = post(serviceUrl + "/Info/sms", requestBody, String.class, new HashMap<String, Object>());
            boolean succeeded = accepted(responseText);
            log.info("短信服务响应已接收，accepted={}", succeeded);
            return succeeded;
        } catch (Exception ee) {
            log.warn("短信发送失败", ee);
        }
        return false;
    }

    /**
     * 接收短信
     * @param content
     * @return
     * @throws IfException
     */
    public boolean receiveMsg(String mobile, String content) throws IfException {
        try {
            Map<String, Object> requestBody = new HashMap<String, Object>();
            requestBody.put("Action", "receive");
            requestBody.put("mobile", mobile);
            requestBody.put("content", content);
            String responseText = post(serviceUrl + "/Info/sms", requestBody, String.class, new HashMap<String, Object>());
            boolean succeeded = accepted(responseText);
            log.debug("短信记录服务响应已接收，accepted={}", succeeded);
            return succeeded;
        } catch (Exception ee) {
            log.warn("短信接收失败", ee);
        }
        return false;
    }


    /**
     * 查询短信列表
     * @return 短信集合
     */
    public List findSMSList() {
        Map map = new HashMap();
        map.put("id", 1);
        map.put("title", "我是标题");
        map.put("content", "我是内容");
        List list = get(serviceUrl + "/Info/sms?id={id}&title={title}&content={content}", ArrayList.class, map);//一：此种方法可以亦可以传值,按照map变量值匹配
        return list;
    }

    /**
     * 根据id查询短信信息
     * @param id
     * @return
     */
    public Sms findSmsById(Integer id) {
        Sms sms = null;
        try {
            sms = get(serviceUrl + "/Info/sms/{id}", Sms.class, id);
            if (sms != null) {
                log.debug("短信查询完成，id={}, contentPresent={}, createdAtPresent={}", sms.getId(),
                        sms.getContent() != null && !sms.getContent().isEmpty(), sms.getCreateTime() != null);
            }
        } catch (Exception ee) {
            log.warn("邮件发送失败", ee);
        }
        return sms;
    }


    /**
     * 修改短信内容
    q     * @return
     */
    public Sms updateSms(Sms sms) {
        String result = put(serviceUrl + "/Info/sms",sms,String.class);
        log.info("值修改后：" + sms.getDraft());
        log.info("result:" + result);
        return sms;
    }

    /**
     * 根据id查询邮件信息
     * @param id
     * @return
     */
    public Email findEmailById(Integer id) {
        Email email = null;
        try {
            email = get(serviceUrl + "/Info/email/{id}", Email.class, id);
            if (email != null) {
                log.debug("邮件查询完成，createdAtPresent={}", email.getCreateTime() != null);
            }
        } catch (Exception ee) {
            log.warn("短信查询失败", ee);
        }
        return email;
    }


    /**
     * 发送邮件
     * @param content
     * @return
     * @throws IfException
     */
    public boolean sendEmail(String receiveAccount, String title, String content) throws IfException {
        try {
            Map<String, Object> requestBody = new HashMap<String, Object>();
            requestBody.put("emailAccount", receiveAccount);
            requestBody.put("title", title);
            requestBody.put("content", content);
            String responseText = post(serviceUrl + "/Info/email", requestBody, String.class, new HashMap<String, Object>());
            boolean succeeded = accepted(responseText);
            log.debug("邮件服务响应已接收，accepted={}", succeeded);
            return succeeded;
        } catch (Exception ee) {
            log.warn("邮件查询失败", ee);
        }
        return false;

    }

    /** A transport-level 200 is not delivery success; require the shared envelope's success payload. */
    private boolean accepted(String responseText) {
        if (responseText == null || responseText.trim().isEmpty()) return false;
        try {
            JsonNode response = objectMapper.readTree(responseText);
            return response.path("status").asInt(0) >= 200
                    && response.path("status").asInt(0) < 300
                    && "success".equalsIgnoreCase(response.path("data").asText());
        } catch (Exception exception) {
            log.warn("消息服务返回非预期响应格式");
            return false;
        }
    }

    /**
     * 查询邮件列表
     * @return 邮件集合
     */
    public List findEmailList() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("id", "1");
        map.put("title", "我是标题");
        map.put("content", "我是内容");
        List list = get(serviceUrl + "/Info/email?id={id}&title={title}&content={content}", ArrayList.class, map);
        return list;
    }


    /**
     * 修改邮件信息
     * @return
     */
    public Email updateEmail(Email email) {
        log.info("邮件更新请求已构造");
        email =  put(serviceUrl + "/Info/email",email,Email.class);
        log.info("邮件更新请求已完成，resultPresent={}", email != null);
        return email;
    }

    /**
     * 查询极光信息
     * @return
     */
    public Jgpush findJgById(Integer id) {
        log.info("查询极光信息");
        Map<String, String> map = new HashMap<String, String>();
        map.put("id", "1");
        Jgpush jp = get(serviceUrl + "/Info/jgpush/{id}", Jgpush.class, map);
        return jp;
    }

    /**
     * 查询极光列表
     * @return
     */
    public List<Jgpush> findJgpushs() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("title", "标题");
        map.put("content", "内容");
        List<Jgpush> list = get(serviceUrl + "/Info/jgpush/{content}/{content}", ArrayList.class, map);
        return list;
    }

    /**
     * 发送极光信息
     * @param title
     * @param content
     * @return
     */
    public String sendJgpush(String title, String content, String platform, String sendId, String releaseFun) {
        Map<String, Object> requestBody = new HashMap<String, Object>();
        requestBody.put("platform", platform);
        requestBody.put("sendId", sendId);
        requestBody.put("releaseFun", releaseFun);
        requestBody.put("title", title);
        requestBody.put("content", content);
        log.info("极光推送请求已构造，platformPresent={}, recipientPresent={}, releaseFunctionPresent={}",
                platform != null, sendId != null, releaseFun != null);
        String result = post(serviceUrl + "/Info/jgpush", requestBody, String.class, new HashMap<String, Object>());
        log.info("请求响应结果：" + result);
        return result;
    }


    /**
     * 查询公共信息列表
     * @return 信息集合
     */
    public List findCommonInfoList() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("id", "1");
        map.put("title", "我是标题");
        map.put("content", "a");
        map.put("sendAccount", "132");
        map.put("receiveAccount", "1112");
        log.debug("公共通知信息查询请求已构造");
        String param = "/Info?id={id}&title={title}";
        param += "&content={content}&sendAccount={sendAccount}&receiveAccount={receiveAccount}";
        List list = get(serviceUrl + param, List.class, map);
        return list;
    }


}
