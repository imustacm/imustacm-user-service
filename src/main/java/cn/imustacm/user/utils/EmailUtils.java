package cn.imustacm.user.utils;

import cn.imustacm.user.model.Option;
import cn.imustacm.user.service.OptionService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * @author wangjianli
 * @date 2019-08-03 20:57
 */
@Component
public class EmailUtils {

    @Autowired
    private OptionService optionService;

    public void sendEmail(String mailTo, String subject, String context) throws MessagingException {
        Option option = optionService.getByKey("mail");
        JSONObject value = JSONObject.parseObject(option.getValue());
        String host = value.getString("host");
        String nickname = value.getString("nickname");
        String username = value.getString("username");
        String password = value.getString("password");

        Properties props = new Properties();
        props.put("mail.smtp.host", host);//设置发送邮件的邮件服务器的属性
        props.put("mail.smtp.auth", "true");  //需要经过授权，也就是有户名和密码的校验，这样才能通过验证
        Session session = Session.getDefaultInstance(props);//用props对象构建一个session
        session.setDebug(true);
        MimeMessage message = new MimeMessage(session);//用session为参数定义消息对象
        String nick = "";
        try {
            nick=javax.mail.internet.MimeUtility.encodeText(nickname);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        message.setFrom(new InternetAddress(nick+" <"+username+">"));//加载发件人地址
        String[] mailTos = null;
        mailTos = mailTo.split(",");
        InternetAddress[] sendTo = new InternetAddress[mailTos.length];//加载收件人地址
        for (int i = 0; i < mailTos.length; i++) {
            sendTo[i] = new InternetAddress(mailTos[i]);
        }

        message.addRecipients(Message.RecipientType.TO,sendTo);
//        message.addRecipients(MimeMessage.RecipientType.CC, InternetAddress.parse(FROM));//设置在发送给收信人之前给自己（发送方）抄送一份，不然会被当成垃圾邮件，报554错
        message.setSubject(subject);//加载标题
        Multipart multipart = new MimeMultipart();//向multipart对象中添加邮件的各个部分内容，包括文本内容和附件
        BodyPart contentPart = new MimeBodyPart();//设置邮件的文本内容
        contentPart.setText(context);
        multipart.addBodyPart(contentPart);
//        if(!AFFIX.isEmpty()){//添加附件
//            BodyPart messageBodyPart = new MimeBodyPart();
//            DataSource source = new FileDataSource(AFFIX);
//            messageBodyPart.setDataHandler(new DataHandler(source));//添加附件的内容
//            sun.misc.BASE64Encoder enc = new sun.misc.BASE64Encoder();//添加附件的标题
//            messageBodyPart.setFileName("=?GBK?B?"+ enc.encode(AFFIXNAME.getBytes()) + "?=");
//            multipart.addBodyPart(messageBodyPart);
//        }
        message.setContent(multipart);//将multipart对象放到message中
        message.saveChanges(); //保存邮件
        Transport transport = session.getTransport("smtp");//发送邮件
        transport.connect(host, username, password);//连接服务器的邮箱
        transport.sendMessage(message, message.getAllRecipients());//把邮件发送出去
        transport.close();//关闭连接
    }
}
