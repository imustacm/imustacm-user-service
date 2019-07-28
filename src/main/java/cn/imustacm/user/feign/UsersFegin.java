package cn.imustacm.user.feign;

import cn.imustacm.common.domain.Resp;
import cn.imustacm.user.dto.CaptchaDTO;
import cn.imustacm.user.dto.RegisterDTO;
import cn.imustacm.user.dto.UserBaseInfoDTO;
import cn.imustacm.user.model.Users;
import cn.imustacm.user.service.IUsersService;
import cn.imustacm.user.service.UsersService;
import cn.imustacm.user.utils.Captcha;
import cn.imustacm.user.utils.GifCaptcha;
import cn.imustacm.user.utils.RedisUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
public class UsersFegin implements IUsersService {


    @Autowired
    private UsersService usersService;

    @Autowired
    RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    RedisConnectionFactory redisConnectionFactory;


    @Override
    public Resp register(RegisterDTO registerDTO) {

        // 在这里完成注册逻辑

        // 入库前最后一步----->将dto对象转换成实体类对象
        Users user = Users.builder()
                .username(registerDTO.getUsername())
                .password("加密后的密码")
                .realname(registerDTO.getName())
                .build();
        return Resp.ok(usersService.save(user));
    }

    @Override
    public Resp getVerification() {
        redisTemplate = RedisUtils.redisTemplate(redisConnectionFactory);
        Captcha captcha = new GifCaptcha(146, 33, 4);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        String base64 = captcha.out(bos);
        String word = captcha.text().toLowerCase();
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        String key = "Code:" + uuid;
        redisTemplate.opsForValue().set(key, word, 300, TimeUnit.SECONDS);
        return Resp.ok(CaptchaDTO.builder().key(key).value(base64).build());
    }

    @Override
    public Resp getUserList(Integer pageIndex, Integer pageSize) {
        if (Objects.isNull(pageIndex) || Objects.isNull(pageSize)) {
            return Resp.ok();
        }
        Page<Users> page = usersService.getList(pageIndex, pageSize);
        List<Users> userList = page.getRecords();
        if (CollectionUtils.isEmpty(userList)) {
            return Resp.ok(new Page<>());
        }
        // 实体类转换成dto
        List<UserBaseInfoDTO> userBaseInfoDTOList = userList.stream()
                .map(users -> UserBaseInfoDTO
                        .builder()
                        .name(users.getRealname())
                        .username(users.getUsername())
                        .school(users.getSchool())
                        .build())
                .collect(Collectors.toList());

        // 将dto集合封装到分页对象
        Page<UserBaseInfoDTO> result = new Page<>(page.getCurrent(), userBaseInfoDTOList.size(), page.getTotal());
        result.setRecords(userBaseInfoDTOList);
        return Resp.ok(result);
    }


}