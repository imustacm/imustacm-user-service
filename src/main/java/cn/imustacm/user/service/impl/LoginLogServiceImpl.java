package cn.imustacm.user.service.impl;

import cn.imustacm.user.mapper.LoginLogMapper;
import cn.imustacm.user.model.LoginLog;
import cn.imustacm.user.service.LoginLogService;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 服务实现类
 *
 * @author wangjianli
 * @since 2019-07-31
 */
@Service
public class LoginLogServiceImpl extends ServiceImpl<LoginLogMapper, LoginLog> implements LoginLogService {

}
