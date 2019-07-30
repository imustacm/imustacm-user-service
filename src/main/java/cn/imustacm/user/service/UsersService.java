package cn.imustacm.user.service;

import cn.imustacm.user.model.Users;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;



import java.util.List;

/**
 * 服务类
 *
 * @author liandong
 * @since 2019-07-21
 */
public interface UsersService extends IService<Users> {

    /**
     * 分页查询用户信息
     *
     * @param pageIndex
     * @param pageSize
     * @return
     */
    Page<Users> getList(Integer pageIndex, Integer pageSize);

    /**
     * 根据用户名查询用户信息
     *
     * @param username
     * @return
     */
    Users getByUsername(String username);
}
