package cn.imustacm.user.feign;

import cn.imustacm.common.domain.Resp;
import cn.imustacm.common.enums.ErrorCodeEnum;
import cn.imustacm.user.dto.*;
import cn.imustacm.user.model.LoginLog;
import cn.imustacm.user.model.Users;
import cn.imustacm.user.service.IUsersService;
import cn.imustacm.user.service.LoginLogService;
import cn.imustacm.user.service.UsersService;
import cn.imustacm.user.utils.*;
import com.auth0.jwt.interfaces.Claim;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiniu.util.Auth;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    EmailUtils emailUtils;
    @Autowired
    RedisConnectionFactory redisConnectionFactory;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Value("${jwt.header}")
    private String header;
    @Value("${jwt.prefix}")
    private String prefix;
    @Value("${mail.web-url}")
    private String weburl;

    @Value("${qiniu.accessKey}")
    private String accessKey;
    @Value("${qiniu.secretKey}")
    private String secretKey;
    @Value("${qiniu.bucket}")
    private String bucket;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 获取验证码
     */
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
        return Resp.ok(CaptchaDTO.builder().key(uuid).value(bytes).build());
    }

    /**
     * 获取七牛云Token
     */
    @Override
    public Resp getQiniuToken() {
        Auth auth = Auth.create(accessKey, secretKey);
        String token = auth.uploadToken(bucket);
        return Resp.ok(QiniuDTO.builder().token(token).build());
    }

    /**
     * 用户注册
     */
    @Override
    public Resp register(RegisterDTO registerDTO) {
        redisTemplate = RedisUtils.redisTemplate(redisConnectionFactory);
        //key为空
        if(registerDTO.getCaptchaKey() == null || "".equals(registerDTO.getCaptchaKey()))
            return Resp.fail(ErrorCodeEnum.USER_VERIFICATION_EXPIRED);
        String key = "Code:" + registerDTO.getCaptchaKey();
        boolean hasKey = redisTemplate.hasKey(key);
        //key在redis中不存在
        if(!hasKey)
            return Resp.fail(ErrorCodeEnum.USER_VERIFICATION_EXPIRED);
        //value为空
        if(registerDTO.getCaptchaValue() == null || "".equals(registerDTO.getCaptchaValue()))
            return Resp.fail(ErrorCodeEnum.USER_VERIFICATION_EMPTY);
        //根据key获取redis中的验证码
        String img = redisTemplate.opsForValue().get(key).toString();
        redisTemplate.delete(key);
        //验证码不匹配
        if(!img.equals(registerDTO.getCaptchaValue().toLowerCase()))
            return Resp.fail(ErrorCodeEnum.USER_VERIFICATION_ERROR);
        //用户名为空
        if(registerDTO.getUsername() == null || "".equals(registerDTO.getUsername()))
            return Resp.fail(ErrorCodeEnum.USER_USERNAME_EMPTY);
        //正则校验用户名为6-20位数字字母组合
        String rexUsername = "^([A-Z]|[a-z]|[0-9]){6,20}$";
        boolean isUsernameMatch = Pattern.matches(rexUsername, registerDTO.getUsername());
        //用户名格式不合法
        if(!isUsernameMatch)
            return Resp.fail(ErrorCodeEnum.USER_USERNAME_ILLEGAL);
        //密码为空
        if(registerDTO.getPassword() == null || "".equals(registerDTO.getPassword()))
            return Resp.fail(ErrorCodeEnum.USER_PASSWORD_EMPTY);
        //正则校验密码为6-20位数字字母特殊字符组合
        String rexPassword = "^([A-Z]|[a-z]|[0-9]|[`=\\[\\]\\-;,./~!@#$%^*()_+}{:?]){6,20}$";
        boolean isPasswordMatch = Pattern.matches(rexPassword, registerDTO.getPassword());
        //密码格式不合法
        if(!isPasswordMatch)
            return Resp.fail(ErrorCodeEnum.USER_PASSWORD_ILLEGAL);
        //重复密码为空
        if(registerDTO.getRePassword() == null || "".equals(registerDTO.getRePassword()))
            return Resp.fail(ErrorCodeEnum.USER_REPASSWORD_EMPTY);
        //两次密码输入不一致
        if(!registerDTO.getRePassword().equals(registerDTO.getPassword()))
            return Resp.fail(ErrorCodeEnum.USER_INCONSISTENT_PASSWORDS);
        //姓名为空
        if(registerDTO.getName() == null || "".equals(registerDTO.getName()))
            return Resp.fail(ErrorCodeEnum.USER_NAME_EMPTY);
        //姓名格式不合法
        if(registerDTO.getName().length() < 2 || registerDTO.getName().length() > 16)
            return Resp.fail(ErrorCodeEnum.USER_NAME_ILLEGAL);
        Users users = usersService.getByUsername(registerDTO.getUsername());
        //用户已经存在
        if(users != null)
            return Resp.fail(ErrorCodeEnum.USER_USER_EXIST);
        String password = bCryptPasswordEncoder.encode(registerDTO.getPassword());
        LocalDateTime localDateTime = LocalDateTime
                .parse(LocalDateTime.now().format(DATE_TIME_FORMATTER), DATE_TIME_FORMATTER);
        String ip = "127.0.0.1";  //ip先写死
        //写入数据库
        Users user = Users.builder()
                .username(registerDTO.getUsername())
                .password(password)
                .realname(registerDTO.getName())
                .regtime(localDateTime)
                .regip(ip)
                .visible(true)
                .emailflag(false)
                .build();
        boolean saveFlag = usersService.save(user);
        if(!saveFlag)
            return Resp.fail(ErrorCodeEnum.FAIL);
        return Resp.ok();
    }

    /**
     * 用户登录
     */
    @Override
    public Resp login(HttpServletResponse response, LoginDTO loginDTO) {
        redisTemplate = RedisUtils.redisTemplate(redisConnectionFactory);
        if(loginDTO.getCaptchaKey() == null || "".equals(loginDTO.getCaptchaKey()))
            return Resp.fail(ErrorCodeEnum.USER_VERIFICATION_EXPIRED);
        String key = "Code:" + loginDTO.getCaptchaKey();
        boolean hasKey = redisTemplate.hasKey(key);
        if(!hasKey)
            return Resp.fail(ErrorCodeEnum.USER_VERIFICATION_EXPIRED);
        if(loginDTO.getCaptchaValue() == null || "".equals(loginDTO.getCaptchaValue()))
            return Resp.fail(ErrorCodeEnum.USER_VERIFICATION_EMPTY);
        String img = redisTemplate.opsForValue().get(key).toString();
        redisTemplate.delete(key);
        if(!img.equals(loginDTO.getCaptchaValue().toLowerCase()))
            return Resp.fail(ErrorCodeEnum.USER_VERIFICATION_ERROR);
        if(loginDTO.getUsername() == null || "".equals(loginDTO.getUsername()))
            return Resp.fail(ErrorCodeEnum.USER_USERNAME_EMPTY);
        if(loginDTO.getPassword() == null || "".equals(loginDTO.getPassword()))
            return Resp.fail(ErrorCodeEnum.USER_PASSWORD_EMPTY);
        Users users = usersService.getByUsername(loginDTO.getUsername());
        if(users == null)
            return Resp.fail(ErrorCodeEnum.USER_USERINFO_ERROR);
        int id = users.getId();
        String username = users.getUsername();
        String now = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        LocalDateTime localDateTime = LocalDateTime.parse(now, DATE_TIME_FORMATTER);
        String ip = "127.0.0.1";  //ip先写死
        boolean flag = bCryptPasswordEncoder.matches(loginDTO.getPassword(), users.getPassword());
        //密码不正确
        if(!flag) {
            LoginLog loginLog = LoginLog.builder()
                    .userid(id)
                    .createtime(localDateTime)
                    .ip(ip)
                    .visible(false)
                    .build();
            boolean saveFlag = loginLogService.save(loginLog);
            if(!saveFlag)
                return Resp.fail(ErrorCodeEnum.FAIL);
            return Resp.fail(ErrorCodeEnum.USER_USERINFO_ERROR);
        }
        //获取token
        String token = jwtUtils.createLoginToken(id, username, now, ip);
        LoginLog loginLog = LoginLog.builder()
                .userid(id)
                .createtime(localDateTime)
                .ip(ip)
                .visible(true)
                .build();
        boolean saveFlag = loginLogService.save(loginLog);
        if(!saveFlag)
            return Resp.fail(ErrorCodeEnum.FAIL);
        redisTemplate.opsForValue().set("Login:" + token, id);
        response.setHeader(header, prefix + token);
        return Resp.ok();
    }

    /**
     * 绑定Email
     */
    @Override
    public Resp bindEmail(HttpServletRequest request, BindEmailDTO bindEmailDTO) {
        try {
            String req = request.getHeader(header).replace(prefix, "");
            Map<String, Claim> map = jwtUtils.verifyToken(req);
            String now = LocalDateTime.now().format(DATE_TIME_FORMATTER);
            LocalDateTime localDateTime = LocalDateTime.parse(now, DATE_TIME_FORMATTER);
            int id = map.get("id").asInt();
            Users users = usersService.getById(id);
            if(users == null)
                return Resp.fail(ErrorCodeEnum.FAIL);
            LocalDateTime time = users.getEmaicltime();
            if(time != null) {
                //获取当前时间
                long minus = (int) Duration.between(time, localDateTime).toMillis();
                minus /= 1000;
                if(minus <= 300)
                    return Resp.fail(ErrorCodeEnum.USER_EMAIL_SEND_TIME);
            }

            String email = bindEmailDTO.getEmail();
            String rexEmail =  "^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$";
            boolean isEmailMatch = Pattern.matches(rexEmail, email);
            //Email格式不合法
            if(!isEmailMatch)
                return Resp.fail(ErrorCodeEnum.USER_EMAIL_ILLEGAL);
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            redisTemplate = RedisUtils.redisTemplate(redisConnectionFactory);
            redisTemplate.opsForValue().set("Email:" + uuid, id, 300, TimeUnit.SECONDS);
            String url = weburl + uuid;
            String context = "您好，感谢您使用IMUSTACM！\n       "
                    + "请点击以下链接以完成您的邮箱验证：\n       "
                    + url + "\n       "
                    + "如果不能点击该链接地址，请复制并粘贴到浏览器的地址输入框中访问。\n       "
                    + "该链接5分钟内有效，请不要将链接地址泄露给其他人员，以免造成不必要的损失。\n       "
                    + "该邮件为系统自动发出，请勿直接回复。如有问题，请与IMUSTACM管理员联系，谢谢。\n"
                    + "                                                           "
                    + "IMUSTACM\n"
                    + "                                                   "
                    + now;
            emailUtils.sendEmail(email, "IMUSTACM 邮箱验证", context);
            //更新数据库
            Users user = Users.builder()
                    .id(id)
                    .email(email)
                    .emaicltime(localDateTime)
                    .emailflag(false)
                    .build();
            boolean saveFlag = usersService.updateById(user);
            if(!saveFlag)
                return Resp.fail(ErrorCodeEnum.FAIL);
        } catch (MessagingException e) {
            return Resp.fail(ErrorCodeEnum.USER_EMAIL_SEND_ERROR);
        } catch (Exception e) {
            return Resp.fail(ErrorCodeEnum.USER_LOGIN_STATUS);
        }
        return Resp.ok();
    }

    /**
     * 验证Email
     */
    @Override
    public Resp verifyEmail(String id) {
        String now = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        LocalDateTime localDateTime = LocalDateTime.parse(now, DATE_TIME_FORMATTER);
        redisTemplate = RedisUtils.redisTemplate(redisConnectionFactory);
        //key为空
        if(id == null || "".equals(id))
            return Resp.fail(ErrorCodeEnum.USER_EMAIL_LINK_ILLEGAL);
        String key = "Email:" + id;
        boolean hasKey = redisTemplate.hasKey(key);
        //key在redis中不存在
        if(!hasKey)
            return Resp.fail(ErrorCodeEnum.USER_EMAIL_LINK_ILLEGAL);
        int userid = Integer.valueOf(redisTemplate.opsForValue().get(key).toString());
        redisTemplate.delete(key);
        //更新数据库
        Users user = Users.builder()
                .id(userid)
                .emailutime(localDateTime)
                .emailflag(true)
                .build();
        boolean saveFlag = usersService.updateById(user);
        if(!saveFlag)
            return Resp.fail(ErrorCodeEnum.FAIL);
        return Resp.ok();
    }

    /**
     * 分页获取用户列表
     */
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