package cn.imustacm.user.controller;


import cn.imustacm.common.domain.Resp;
import cn.imustacm.user.dto.CaptchaDTO;
import cn.imustacm.user.utils.Captcha;
import cn.imustacm.user.utils.GifCaptcha;
import cn.imustacm.common.utils.RedisUtils;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码相关
 *
 * @author liandong
 * @date 2019/08/18
 */
@RestController
@RequestMapping("/verification")
public class VerificationController {

    @Autowired
    RedisConnectionFactory redisConnectionFactory;

    @Autowired
    RedisTemplate<Object, Object> redisTemplate;


    /**
     * 获取验证码
     *
     * @return
     */
    @GetMapping("/get")
    public Resp getVerification() {
        redisTemplate = RedisUtils.redisTemplate(redisConnectionFactory);
        Captcha captcha = new GifCaptcha(146, 33, 4);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        String base64 = captcha.out(bos);
        byte[] bytes = Base64.decode(base64);
        String word = captcha.text().toLowerCase();
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        String key = "Code:" + uuid;
        redisTemplate.opsForValue().set(key, word, 300, TimeUnit.SECONDS);
        return Resp.ok(CaptchaDTO.builder().key(uuid).value(bytes).build());
    }

}
