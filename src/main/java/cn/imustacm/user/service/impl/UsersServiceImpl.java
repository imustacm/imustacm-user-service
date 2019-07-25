package cn.imustacm.user.service.impl;

import cn.imustacm.user.mapper.UsersMapper;
import cn.imustacm.user.model.Users;
import cn.imustacm.user.service.UsersService;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author liandong
 * @since 2019-07-21
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements UsersService {

}
