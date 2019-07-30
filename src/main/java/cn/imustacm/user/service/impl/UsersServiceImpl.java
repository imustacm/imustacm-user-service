package cn.imustacm.user.service.impl;

import cn.imustacm.user.mapper.UsersMapper;
import cn.imustacm.user.model.Users;
import cn.imustacm.user.service.UsersService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 服务实现类
 *
 * @author liandong
 * @since 2019-07-21
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements UsersService {

    @Override
    public Page<Users> getList(Integer pageIndex, Integer pageSize) {
        if (pageIndex < 0){
            pageIndex = 1;
        }
        if(pageSize < 0 ){
            pageSize = 20;
        }
        LambdaQueryWrapper<Users> wrapper = new QueryWrapper<Users>().lambda().orderByAsc(Users::getId);
        return (Page<Users>) page(new Page<>(pageIndex, pageSize), wrapper);
    }

    @Override
    public Users getByUsername(String username) {
        LambdaQueryWrapper<Users> wrapper = new QueryWrapper<Users>().lambda().eq(Users::getUsername, username);
        Users users = getOne(wrapper);
        return users;
    }
}
