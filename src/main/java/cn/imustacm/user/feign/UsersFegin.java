package cn.imustacm.user.feign;

import cn.imustacm.common.domain.Resp;
import cn.imustacm.common.enums.ErrorCodeEnum;
import cn.imustacm.user.dto.*;
import cn.imustacm.user.model.LoginLog;
import cn.imustacm.user.model.Users;
import cn.imustacm.user.service.IUsersService;
import cn.imustacm.user.service.LoginLogService;
import cn.imustacm.user.service.UsersService;
import cn.imustacm.user.utils.Captcha;
import cn.imustacm.user.utils.GifCaptcha;
import cn.imustacm.user.utils.JwtUtils;
import cn.imustacm.user.utils.RedisUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.imustacm.common.consts.DatePatternConst.DATE_TIME_FORMATTER;

@RestController
public class UsersFegin implements IUsersService {

    @Autowired
    private UsersService usersService;
    @Autowired
    private LoginLogService loginLogService;
    @Autowired
    RedisTemplate<Object, Object> redisTemplate;
    @Autowired
    JwtUtils jwtUtils;
    @Autowired
    RedisConnectionFactory redisConnectionFactory;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public Resp register(RegisterDTO registerDTO) {
        redisTemplate = RedisUtils.redisTemplate(redisConnectionFactory);
        if(registerDTO.getCaptchaKey() == null || "".equals(registerDTO.getCaptchaKey()))
            return Resp.fail(ErrorCodeEnum.USER_VERIFICATION_EXPIRED);
        String key = "Code:" + registerDTO.getCaptchaKey();
        boolean hasKey = redisTemplate.hasKey(key);
        if(!hasKey)
            return Resp.fail(ErrorCodeEnum.USER_VERIFICATION_EXPIRED);
        String img = redisTemplate.opsForValue().get(key).toString();
        redisTemplate.delete(key);
        if(registerDTO.getCaptchaValue() == null || "".equals(registerDTO.getCaptchaValue()))
            return Resp.fail(ErrorCodeEnum.USER_VERIFICATION_EMPTY);
        if(!img.equals(registerDTO.getCaptchaValue().toLowerCase()))
            return Resp.fail(ErrorCodeEnum.USER_VERIFICATION_ERROR);
        if(registerDTO.getUsername() == null || "".equals(registerDTO.getUsername()))
            return Resp.fail(ErrorCodeEnum.USER_USERNAME_EMPTY);
        String regUsername = "^([A-Z]|[a-z]|[0-9]){6,20}$";
        boolean isUsernameMatch = Pattern.matches(regUsername, registerDTO.getUsername());
        if(!isUsernameMatch)
            return Resp.fail(ErrorCodeEnum.USER_USERNAME_ILLEGAL);
        if(registerDTO.getPassword() == null || "".equals(registerDTO.getPassword()))
            return Resp.fail(ErrorCodeEnum.USER_PASSWORD_EMPTY);
        String regPassword = "^([A-Z]|[a-z]|[0-9]|[`=\\[\\]\\-;,./~!@#$%^*()_+}{:?]){6,20}$";
        boolean isPasswordMatch = Pattern.matches(regPassword, registerDTO.getPassword());
        if(!isPasswordMatch)
            return Resp.fail(ErrorCodeEnum.USER_PASSWORD_ILLEGAL);
        if(registerDTO.getRePassword() == null || "".equals(registerDTO.getRePassword()))
            return Resp.fail(ErrorCodeEnum.USER_REPASSWORD_EMPTY);
        if(!registerDTO.getRePassword().equals(registerDTO.getPassword()))
            return Resp.fail(ErrorCodeEnum.USER_INCONSISTENT_PASSWORDS);
        if(registerDTO.getName() == null || "".equals(registerDTO.getName()))
            return Resp.fail(ErrorCodeEnum.USER_NAME_EMPTY);
        Users users = usersService.getByUsername(registerDTO.getUsername());
        if(users != null)
            return Resp.fail(ErrorCodeEnum.USER_USER_EXIST);
        String password = bCryptPasswordEncoder.encode(registerDTO.getPassword());
        LocalDateTime localDateTime = LocalDateTime
                .parse(LocalDateTime.now().format(DATE_TIME_FORMATTER), DATE_TIME_FORMATTER);
        String ip = "127.0.0.1";
        Users user = Users.builder()
                .username(registerDTO.getUsername())
                .password(password)
                .realname(registerDTO.getName())
                .regtime(localDateTime)
                .regip(ip)
                .visible(true)
                .build();
        boolean saveFlag = usersService.save(user);
        if(!saveFlag)
            return Resp.fail(ErrorCodeEnum.FAIL);
        return Resp.ok();
    }

    @Override
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
        return Resp.ok(CaptchaOutDTO.builder().key(uuid).value(bytes).build());
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


    @Override
    public Resp login(LoginDTO loginDTO) {
        redisTemplate = RedisUtils.redisTemplate(redisConnectionFactory);
        if(loginDTO.getCaptchaKey() == null || "".equals(loginDTO.getCaptchaKey()))
            return Resp.fail(ErrorCodeEnum.USER_VERIFICATION_EXPIRED);
        String key = "Code:" + loginDTO.getCaptchaKey();
        boolean hasKey = redisTemplate.hasKey(key);
        if(!hasKey)
            return Resp.fail(ErrorCodeEnum.USER_VERIFICATION_EXPIRED);
        String img = redisTemplate.opsForValue().get(key).toString();
        redisTemplate.delete(key);
        if(loginDTO.getCaptchaValue() == null || "".equals(loginDTO.getCaptchaValue()))
            return Resp.fail(ErrorCodeEnum.USER_VERIFICATION_EMPTY);
        if(!img.equals(loginDTO.getCaptchaValue().toLowerCase()))
            return Resp.fail(ErrorCodeEnum.USER_VERIFICATION_ERROR);
        if(loginDTO.getUsername() == null || "".equals(loginDTO.getUsername()))
            return Resp.fail(ErrorCodeEnum.USER_USERNAME_EMPTY);
        if(loginDTO.getPassword() == null || "".equals(loginDTO.getPassword()))
            return Resp.fail(ErrorCodeEnum.USER_PASSWORD_EMPTY);
        Users users = usersService.getByUsername(loginDTO.getUsername());
        if(users == null)
            return Resp.fail(ErrorCodeEnum.USER_USERINFO_ERROR);
        boolean flag = bCryptPasswordEncoder.matches(loginDTO.getPassword(), users.getPassword());
        if(!flag)
            return Resp.fail(ErrorCodeEnum.USER_USERINFO_ERROR);
        int id = users.getId();
        String username = users.getUsername();
        String now = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        LocalDateTime localDateTime = LocalDateTime.parse(now, DATE_TIME_FORMATTER);
        String ip = "127.0.0.1";
        String token = jwtUtils.createToken(id, username, now, ip);
        LoginLog loginLog = LoginLog.builder()
                .userid(id)
                .createtime(localDateTime)
                .ip(ip)
                .visible(true)
                .build();
        boolean saveFlag = loginLogService.save(loginLog);
        if(saveFlag) {
            redisTemplate.opsForValue().set("Login:" + token, id);
            return Resp.ok(LoginOutDTO.builder().token(token).logintime(now).build());
        } else {
            return Resp.fail(ErrorCodeEnum.FAIL);
        }
    }
}