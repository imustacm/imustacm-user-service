package cn.imustacm.user.service;

import cn.imustacm.user.dto.PermissionDTO;
import cn.imustacm.user.model.SysPermission;
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
public interface SysPermissionService extends IService<SysPermission> {

    /**
     * 获取权限列表
     *
     * @return
     */
    List<PermissionDTO> getList();


    /**
     * 批量获取权限list
     *
     * @param permissionIdList
     * @return
     */
    List<PermissionDTO> batchGetList(List<Integer> permissionIdList);


}
