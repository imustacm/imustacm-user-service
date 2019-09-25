package cn.imustacm.user.controller;

import cn.imustacm.common.domain.Resp;
import cn.imustacm.common.enums.ErrorCodeEnum;
import cn.imustacm.user.dto.CaptchaDTO;
import cn.imustacm.user.utils.Captcha;
import cn.imustacm.user.utils.GifCaptcha;
import cn.imustacm.user.utils.RedisUtils;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.misc.BASE64Encoder;

import javax.servlet.http.Cookie;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import java.net.URL;

/**
 * @author wangjianli
 * @date 2019-09-04 16:04
 */

@RestController
@RequestMapping("/aao")
public class AaoController {

    @Autowired
    RedisTemplate<Object, Object> redisTemplate;
    @Autowired
    RedisConnectionFactory redisConnectionFactory;

    /**
     * 获取验证码
     *
     * @return
     */
    @GetMapping("/getVerification")
    public Resp getVerification() throws Exception {
        redisTemplate = RedisUtils.redisTemplate(redisConnectionFactory);
        URL url = new URL("http://stuzhjw.imust.edu.cn/img/captcha.jpg");
        ByteArrayOutputStream outPut = new ByteArrayOutputStream();
        byte[] data = new byte[10240];
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10 * 1000);
            if(conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return Resp.fail(ErrorCodeEnum.USER_AAO_URL_ILLEGAL);
            }
            String session_value = conn.getHeaderField("Set-Cookie");
            String[] sessionId = session_value.split(";");
            String cookie = sessionId[0];
            InputStream inStream = conn.getInputStream();
            int len = -1;
            while ((len = inStream.read(data)) != -1) {
                outPut.write(data, 0, len);
            }
            inStream.close();
        } catch (IOException e) {
            return Resp.fail(ErrorCodeEnum.USER_AAO_VERIFICATION_GET);
        }
        byte[] bytes = outPut.toByteArray();
        return Resp.ok(CaptchaDTO.builder().value(bytes).build());
    }
}
