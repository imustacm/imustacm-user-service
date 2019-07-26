package cn.imustacm.user.feign;

import cn.imustacm.user.IUsersService;
import cn.imustacm.user.model.Users;
import cn.imustacm.user.service.UsersService;
import cn.imustacm.user.utils.ImageCodeUtils;
import cn.imustacm.user.utils.RedisUtils;
import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    public Boolean register() {
        Users user = Users.builder().username("test").gender(1).build();
        return usersService.save(user);
    }

    @Override
    public JSONObject getVerification() {
        redisTemplate = RedisUtils.redisTemplate(redisConnectionFactory);

        String yy = this.getClass().getClassLoader().getResource("").getPath();
        String path = yy + "/cn/imustacm/user/utils/new_words.txt";
        JSONObject returnJson = ImageCodeUtils.getImage(path);
        String word = returnJson.getString("word");
        String base64Img = returnJson.getString("base64Img");

        // 将验证码内容保存redis
        String key = "Code:" + UUID.randomUUID().toString().replaceAll("-", "");
        redisTemplate.opsForValue().set(key, word, 300, TimeUnit.SECONDS);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", key);
        jsonObject.put("img", Base64.decode(base64Img));
        return jsonObject;
    }
}
