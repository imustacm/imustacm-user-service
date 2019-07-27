package cn.imustacm.user.feign;

import cn.imustacm.user.IUsersService;
import cn.imustacm.user.model.Users;
import cn.imustacm.user.service.UsersService;
import cn.imustacm.user.utils.Captcha;
import cn.imustacm.user.utils.GifCaptcha;
import cn.imustacm.user.utils.RedisUtils;
import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
public class UsersFegin implements IUsersService {

    @Autowired
    private UsersService usersService;
    @Autowired
    RedisTemplate<Object, Object> redisTemplate;
    @Autowired
    RedisConnectionFactory redisConnectionFactory;

    @Override
    public JSONObject register() {
        Users user = Users.builder()
                .username("test")
                .password("123456")
                .gender(1)
                .visible(true)
                .build();
        boolean flag = usersService.save(user);

        JSONObject jsonObject = new JSONObject();
        if(flag) {
            jsonObject.put("code", "0000");
            jsonObject.put("msg", "注册成功！");
        } else {
            jsonObject.put("code", "0100");
            jsonObject.put("msg", "注册失败：无法写入数据库！");
        }
        return jsonObject;
    }

    @Override
    public JSONObject getVerification() {
        redisTemplate = RedisUtils.redisTemplate(redisConnectionFactory);
        Captcha captcha = new GifCaptcha(146,33,4);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        String base64 = captcha.out(bos);
        String word = captcha.text().toLowerCase();
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        String key = "Code:" + uuid;
        redisTemplate.opsForValue().set(key, word, 300, TimeUnit.SECONDS);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "0000");
        jsonObject.put("msg", "获取成功！");
        jsonObject.put("key", key);
        jsonObject.put("img", Base64.decode(base64));
        return jsonObject;
    }
}