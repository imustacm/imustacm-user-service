package cn.imustacm.user.service;

import cn.imustacm.user.dto.InterfaceDTO;
import cn.imustacm.user.dto.PermissionDTO;
import cn.imustacm.user.dto.PermissionInterfaceDTO;
import cn.imustacm.user.model.SysPermissionInterface;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author liandong
 * @since 2020-12-27
 */
public interface SysPermissionInterfaceService extends IService<SysPermissionInterface> {

    /**
     * 保存权限对应接口关联关系
     * 1 删除该权限的旧关联数据
     * 2 保存该权限的新关联数据
     *
     * @param permissionInterfaceDTO
     * @return
     */
    boolean saveInterfaceMapping(PermissionInterfaceDTO permissionInterfaceDTO);


    /**
     * 根据权限id 获取接口列表
     *
     * @param permissionId
     * @return
     */
    List<InterfaceDTO> getInterfaceListByPermissionId(Integer permissionId);

    /**
     * 根据url
     *
     * @param url
     * @return
     */
    Set<String> getInterfacePermissionNameSet(String url);


}
