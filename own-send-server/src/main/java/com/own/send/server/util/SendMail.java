package com.own.send.server.util;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

/**
 * @Description： 发送邮件和接收邮件
 * @author： Blucezhang
 */
public class SendMail {

    public static void sendMail(String email, String title, String content, String iocMailSmtpHost, String iocMailSmtpAuth,
                                String iocSmtp, String iocMailFrom, String iocMailFromUser, String iocMailFromPassWord
    ) throws AddressException, MessagingException {

        Properties props = new Properties();
        props.put("mail.smtp.host", iocMailSmtpHost);
        //指定是否需要SMTP验证
        props.put("mail.smtp.auth", iocMailSmtpAuth);
        Session mailSession = Session.getDefaultInstance(props);
        Transport transport = mailSession.getTransport(iocSmtp);
        mailSession.setDebug(true);//是否在控制台显示debug信息
        Message message = new MimeMessage(mailSession);
        try {
            message.setFrom(new InternetAddress(MimeUtility.encodeText(iocMailFrom), "汇金贷"));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }//发件人
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));//收件人
        message.setSubject(title);//邮件主题
        /** 代表正文的bodypart **/
        MimeBodyPart text = new MimeBodyPart();

        text.setContent(content, "text/html;charset=utf-8");

        /** 代表正文所引用的图片的bodypart **/
              /* MimeBodyPart image = new MimeBodyPart();
               DataHandler dh1 = new DataHandler(new FileDataSource("E:/17.png"));
       	   	  image.setDataHandler(dh1);
       	   	  image.setContentID("17.png");*/

        /** 代表附件的bodypart **/
                   /* MimeBodyPart attach = new MimeBodyPart();
       	   	  DataHandler dh2 = new DataHandler(new FileDataSource("E:杰克逊.wav"));
       	   	  attach.setDataHandler(dh2);
       	   	  //System.out.println(dh2.getName());
       	   	  attach.setFileName(MimeUtility.encodeText(dh2.getName()));   */

        MimeMultipart text_image = new MimeMultipart("related");
        text_image.addBodyPart(text);
        //text_image.addBodyPart(image);
        MimeBodyPart text_image_body = new MimeBodyPart();
        text_image_body.setContent(text_image);

        /** 封装了所有邮件数据的容器 **/
        MimeMultipart multipart = new MimeMultipart("mixed");
        multipart.addBodyPart(text_image_body);

        message.setContent(multipart);
        message.saveChanges();

        transport.connect(iocMailSmtpHost, iocMailFromUser, iocMailFromPassWord);    //这个邮箱可随便使用
        transport.sendMessage(message, message.getAllRecipients());
        transport.close();

    }
}
