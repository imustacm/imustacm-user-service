package cn.imustacm.user.service;

import cn.imustacm.user.dto.PermissionDTO;
import cn.imustacm.user.dto.UserPermissionDTO;
import cn.imustacm.user.model.SysUserPermission;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author liandong
 * @since 2020-12-27
 */
public interface SysUserPermissionService extends IService<SysUserPermission> {


    /**
     * 根据userId获取用户权限列表
     *
     * @param userId
     * @return
     */
    List<PermissionDTO> getPermissionList(Integer userId);

    /**
     * 删除用户权限
     *
     * @param userPermissionDTO
     * @return
     */
    boolean delete(UserPermissionDTO userPermissionDTO);

    /**
     * 新增用户权限
     *
     * @param userPermissionDTO
     * @param createUserId
     * @return
     */
    boolean add(UserPermissionDTO userPermissionDTO, Integer createUserId);

}
