package cn.imustacm.user.controller;

import cn.imustacm.common.consts.GlobalConst;
import cn.imustacm.common.domain.Resp;
import cn.imustacm.common.enums.ErrorCodeEnum;
import cn.imustacm.common.utils.JwtUtils;
import cn.imustacm.user.dto.*;
import cn.imustacm.user.model.LoginLog;
import cn.imustacm.user.model.Option;
import cn.imustacm.user.model.Users;
import cn.imustacm.user.service.*;
import cn.imustacm.user.utils.EmailUtils;
import cn.imustacm.common.utils.RedisUtils;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.imustacm.common.consts.DatePatternConst.DATE_TIME_FORMATTER;

/**
 * 用户相关
 *
 * @author liandong
 * @date 2019/08/18
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UsersService usersService;
    @Autowired
    private LoginLogService loginLogService;
    @Autowired
    RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    EmailUtils emailUtils;
    @Autowired
    RedisConnectionFactory redisConnectionFactory;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private OptionService optionService;

    @Value("${jwt.secret-key}")
    private String jwtSecretKey;

    @Value("${jwt.expire-time}")
    private String jwtExpireTime;

    @Value("${jwt.header}")
    private String header;

    @Autowired
    private SysUserPermissionService userPermissionService;

    @Autowired
    private JwtUtils jwtUtils;

    @Bean
    public JwtUtils jwtUtils() {
        return new JwtUtils(jwtSecretKey, Long.parseLong(jwtExpireTime));
    }


    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 用户注册
     *
     * @return
     */
    @PostMapping("/register")
    public Resp register(@RequestBody RegisterDTO registerDTO) {
        redisTemplate = RedisUtils.redisTemplate(redisConnectionFactory);
        //key为空
        if (registerDTO.getCaptchaKey() == null || "".equals(registerDTO.getCaptchaKey()))
            return Resp.fail(ErrorCodeEnum.USER_VERIFICATION_EXPIRED);
        String key = "Code:" + registerDTO.getCaptchaKey();
        boolean hasKey = redisTemplate.hasKey(key);
        //key在redis中不存在
        if (!hasKey)
            return Resp.fail(ErrorCodeEnum.USER_VERIFICATION_EXPIRED);
        //value为空
        if (registerDTO.getCaptchaValue() == null || "".equals(registerDTO.getCaptchaValue()))
            return Resp.fail(ErrorCodeEnum.USER_VERIFICATION_EMPTY);
        //根据key获取redis中的验证码
        String img = redisTemplate.opsForValue().get(key).toString();
        redisTemplate.delete(key);
        //验证码不匹配
        if (!img.equals(registerDTO.getCaptchaValue().toLowerCase()))
            return Resp.fail(ErrorCodeEnum.USER_VERIFICATION_ERROR);
        //用户名为空
        if (registerDTO.getUsername() == null || "".equals(registerDTO.getUsername()))
            return Resp.fail(ErrorCodeEnum.USER_USERNAME_EMPTY);
        //正则校验用户名为6-20位数字字母组合
        String rexUsername = "^([A-Z]|[a-z]|[0-9]){6,20}$";
        boolean isUsernameMatch = Pattern.matches(rexUsername, registerDTO.getUsername());
        //用户名格式不合法
        if (!isUsernameMatch)
            return Resp.fail(ErrorCodeEnum.USER_USERNAME_ILLEGAL);
        //密码为空
        if (registerDTO.getPassword() == null || "".equals(registerDTO.getPassword()))
            return Resp.fail(ErrorCodeEnum.USER_PASSWORD_EMPTY);
        //正则校验密码为6-20位数字字母特殊字符组合
        String rexPassword = "^([A-Z]|[a-z]|[0-9]|[`=\\[\\]\\-;,./~!@#$%^*()_+}{:?]){6,20}$";
        boolean isPasswordMatch = Pattern.matches(rexPassword, registerDTO.getPassword());
        //密码格式不合法
        if (!isPasswordMatch)
            return Resp.fail(ErrorCodeEnum.USER_PASSWORD_ILLEGAL);
        //重复密码为空
        if (registerDTO.getRePassword() == null || "".equals(registerDTO.getRePassword()))
            return Resp.fail(ErrorCodeEnum.USER_REPASSWORD_EMPTY);
        //两次密码输入不一致
        if (!registerDTO.getRePassword().equals(registerDTO.getPassword()))
            return Resp.fail(ErrorCodeEnum.USER_INCONSISTENT_PASSWORDS);
        //姓名为空
        if (registerDTO.getName() == null || "".equals(registerDTO.getName()))
            return Resp.fail(ErrorCodeEnum.USER_NAME_EMPTY);
        //姓名格式不合法
        if (registerDTO.getName().length() < 2 || registerDTO.getName().length() > 16)
            return Resp.fail(ErrorCodeEnum.USER_NAME_ILLEGAL);
        Users users = usersService.getByUsername(registerDTO.getUsername());
        //用户已经存在
        if (users != null)
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
        if (!saveFlag)
            return Resp.fail(ErrorCodeEnum.FAIL);
        return Resp.ok();
    }


    /**
     * 用户登录
     *
     * @return
     */
    @PostMapping("/login")
    public Resp login(@RequestBody LoginDTO loginDTO) {
        redisTemplate = RedisUtils.redisTemplate(redisConnectionFactory);
        if (loginDTO.getCaptchaKey() == null || "".equals(loginDTO.getCaptchaKey()))
            return Resp.fail(ErrorCodeEnum.USER_VERIFICATION_EXPIRED);
        String key = "Code:" + loginDTO.getCaptchaKey();
        boolean hasKey = redisTemplate.hasKey(key);
        if (!hasKey)
            return Resp.fail(ErrorCodeEnum.USER_VERIFICATION_EXPIRED);
        if (loginDTO.getCaptchaValue() == null || "".equals(loginDTO.getCaptchaValue()))
            return Resp.fail(ErrorCodeEnum.USER_VERIFICATION_EMPTY);
        String img = redisTemplate.opsForValue().get(key).toString();
        redisTemplate.delete(key);
        if (!img.equals(loginDTO.getCaptchaValue().toLowerCase()))
            return Resp.fail(ErrorCodeEnum.USER_VERIFICATION_ERROR);
        if (loginDTO.getUsername() == null || "".equals(loginDTO.getUsername()))
            return Resp.fail(ErrorCodeEnum.USER_USERNAME_EMPTY);
        if (loginDTO.getPassword() == null || "".equals(loginDTO.getPassword()))
            return Resp.fail(ErrorCodeEnum.USER_PASSWORD_EMPTY);
        Users users = usersService.getByUsername(loginDTO.getUsername());
        if (users == null)
            return Resp.fail(ErrorCodeEnum.USER_USERINFO_ERROR);
        int id = users.getId();
        String username = users.getUsername();
        String now = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        LocalDateTime localDateTime = LocalDateTime.parse(now, DATE_TIME_FORMATTER);
        String ip = "127.0.0.1";  //ip先写死
        boolean flag = bCryptPasswordEncoder.matches(loginDTO.getPassword(), users.getPassword());
        //密码不正确
        if (!flag) {
            LoginLog loginLog = LoginLog.builder()
                    .userid(id)
                    .createtime(localDateTime)
                    .ip(ip)
                    .visible(false)
                    .build();
            boolean saveFlag = loginLogService.save(loginLog);
            if (!saveFlag)
                return Resp.fail(ErrorCodeEnum.FAIL);
            return Resp.fail(ErrorCodeEnum.USER_USERINFO_ERROR);
        }
        //获取token
        String token = generateToken(id);
        LoginLog loginLog = LoginLog.builder()
                .userid(id)
                .createtime(localDateTime)
                .ip(ip)
                .visible(true)
                .build();
        boolean saveFlag = loginLogService.save(loginLog);
        if (!saveFlag)
            return Resp.fail(ErrorCodeEnum.FAIL);
        redisTemplate.opsForValue().set("Login:" + token, id, Long.parseLong(jwtExpireTime) / 1000L, TimeUnit.SECONDS);
        return Resp.ok(LoginResultDTO.builder().accessToken(token).build());
    }


    /**
     * 邮箱绑定
     *
     * @return
     */
    @PostMapping("/bindEmail")
    public Resp bindEmail(@RequestBody BindEmailDTO bindEmailDTO,
                          @RequestHeader(value = GlobalConst.USER_ID_HEADER, required = false) Integer userId) {
        try {
            String now = LocalDateTime.now().format(DATE_TIME_FORMATTER);
            LocalDateTime localDateTime = LocalDateTime.parse(now, DATE_TIME_FORMATTER);
            Users users = usersService.getById(userId);
            if (users == null)
                return Resp.fail(ErrorCodeEnum.FAIL);
            LocalDateTime time = users.getEmaicltime();
            if (time != null) {
                //获取当前时间
                long minus = (int) Duration.between(time, localDateTime).toMillis();
                minus /= 1000;
                if (minus <= 300)
                    return Resp.fail(ErrorCodeEnum.USER_EMAIL_SEND_TIME);
            }

            String email = bindEmailDTO.getEmail();
            String rexEmail = "^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$";
            boolean isEmailMatch = Pattern.matches(rexEmail, email);
            //Email格式不合法
            if (!isEmailMatch)
                return Resp.fail(ErrorCodeEnum.USER_EMAIL_ILLEGAL);
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            redisTemplate = RedisUtils.redisTemplate(redisConnectionFactory);
            redisTemplate.opsForValue().set("Email:" + uuid, userId, 300, TimeUnit.SECONDS);
            Option option = optionService.getByKey("mail");
            JSONObject value = JSONObject.parseObject(option.getValue());
            String weburl = value.getString("weburl");
            String url = weburl + uuid;
            String context = "您好，感谢您使用IMUSTACM！\n       "
                    + "请点击以下链接以完成您的邮箱验证：\n       "
                    + url + "\n       "
                    + "该链接5分钟内有效，如果不能点击该链接，请复制并粘贴到浏览器的地址输入框中访问。\n       "
                    + "请不要将链接地址泄露给其他人员。如非本人操作，请勿访问链接地址，以免造成不必要的损失。\n       "
                    + "该邮件为系统自动发出，请勿直接回复。如有问题，请与IMUSTACM管理员联系，感谢您的支持！\n"
                    + "                                                           "
                    + "IMUSTACM\n"
                    + "                                                   "
                    + now;
            emailUtils.sendEmail(email, "IMUSTACM 邮箱验证", context);
            //更新数据库
            Users user = Users.builder()
                    .id(userId)
                    .email(email)
                    .emaicltime(localDateTime)
                    .emailflag(false)
                    .build();
            boolean saveFlag = usersService.updateById(user);
            if (!saveFlag)
                return Resp.fail(ErrorCodeEnum.FAIL);
        } catch (MessagingException e) {
            return Resp.fail(ErrorCodeEnum.USER_EMAIL_SEND_ERROR);
        } catch (Exception e) {
            return Resp.fail(ErrorCodeEnum.USER_LOGIN_STATUS);
        }
        return Resp.ok();
    }

    /**
     * 邮箱验证
     *
     * @return
     */
    @GetMapping("/verifyEmail")
    public Resp verifyEmail(@RequestParam String id) {
        String now = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        LocalDateTime localDateTime = LocalDateTime.parse(now, DATE_TIME_FORMATTER);
        redisTemplate = RedisUtils.redisTemplate(redisConnectionFactory);
        //key为空
        if (StringUtils.isEmpty(id))
            return Resp.fail(ErrorCodeEnum.USER_EMAIL_LINK_ILLEGAL);
        String key = "Email:" + id;
        boolean hasKey = redisTemplate.hasKey(key);
        //key在redis中不存在
        if (!hasKey)
            return Resp.fail(ErrorCodeEnum.USER_EMAIL_LINK_ILLEGAL);
        int userId = Integer.valueOf(redisTemplate.opsForValue().get(key).toString());
        redisTemplate.delete(key);
        //更新数据库
        Users user = Users.builder()
                .id(userId)
                .emailutime(localDateTime)
                .emailflag(true)
                .build();
        boolean saveFlag = usersService.updateById(user);
        if (!saveFlag)
            return Resp.fail(ErrorCodeEnum.FAIL);
        return Resp.ok();
    }

    /**
     * 生成token
     *
     * @param userId
     * @return
     */
    private String generateToken(Integer userId) {
        Map<String, Object> map = new HashMap<>();
        List<PermissionDTO> permissionList = userPermissionService.getPermissionList(userId);
        List<String> permissionNameList = permissionList.stream().map(PermissionDTO::getPermissionName).collect(Collectors.toList());
        String permissionNameListStr = StringUtils.join(permissionNameList, ",");
            map.put(GlobalConst.PERMISSION_NAME_LIST, permissionNameListStr);
        return jwtUtils.createToken(userId.toString(), map);
    }

    /**
     * 获取用户登录后基本信息
     *
     * @return
     */
    @PostMapping("/getLoginInfo")
    public Resp getLoginInfo(@RequestBody LoginResultDTO loginResultDTO) {
        redisTemplate = RedisUtils.redisTemplate(redisConnectionFactory);
        String token = loginResultDTO.getAccessToken();
        if (token == null || "".equals(token))
            return Resp.ok();
        String key = "Login:" + token;
        boolean hasKey = redisTemplate.hasKey(key);
        if (!hasKey)
            return Resp.ok();
        Integer userId = Integer.valueOf(redisTemplate.opsForValue().get(key).toString());
        Users users = usersService.getById(userId);
        if (users == null)
            return Resp.fail(ErrorCodeEnum.SERVER_ERR);
        ArrayList<String> per = new ArrayList<>();
        List<PermissionDTO> permissionDTOS = userPermissionService.getPermissionList(userId);
        per.add("USER");
        for (PermissionDTO perDTO : permissionDTOS) {
            per.add(perDTO.getPermissionName());
        }
        String[] permissions = (String[])per.toArray(new String[per.size()]);
        return Resp.ok(LoginInfoDTO.builder().avatar(users.getAvatar()).username(users.getUsername()).permissions(permissions).build());
    }

    /**
     * 退出登录
     *
     * @return
     */
    @PostMapping("/logout")
    public Resp logout(@RequestHeader(GlobalConst.JWT_HEADER) String token) {
        redisTemplate = RedisUtils.redisTemplate(redisConnectionFactory);
        if (token == null || "".equals(token))
            return Resp.ok();
        String key = "Login:" + token;
        boolean hasKey = redisTemplate.hasKey(key);
        if (!hasKey)
            return Resp.ok();
        redisTemplate.delete(key);
        return Resp.ok();
    }

}
