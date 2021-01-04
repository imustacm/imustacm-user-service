package cn.imustacm.user.controller;

import cn.imustacm.common.consts.GlobalConst;
import cn.imustacm.common.domain.Resp;
import cn.imustacm.user.dto.PermissionDTO;
import cn.imustacm.user.dto.PermissionInterfaceDTO;
import cn.imustacm.user.dto.UserPermissionDTO;
import cn.imustacm.user.model.SysPermission;
import cn.imustacm.user.service.SysPermissionInterfaceService;
import cn.imustacm.user.service.SysPermissionService;
import cn.imustacm.user.service.SysUserPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 权限管理controller
 */

@RestController
@RequestMapping("/permission")
@Slf4j
public class PermissionController {

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private SysPermissionInterfaceService permissionInterfaceService;

    @Autowired
    private SysUserPermissionService sysUserPermissionService;


    /**
     * 新增或者保存权限
     *
     * @param permissionDTO
     * @return
     */
    @PostMapping("save/or/update")
    public Resp saveOrUpdate(@RequestBody @Validated PermissionDTO permissionDTO) {
        SysPermission permission = SysPermission
                .builder()
                .id(permissionDTO.getId())
                .permissionName(permissionDTO.getPermissionName())
                .description(permissionDTO.getDescription())
                .build();
        boolean flag = permissionService.saveOrUpdate(permission);
        return Resp.okOrFail(flag);
    }


    /**
     * 获取权限列表
     *
     * @return
     */
    @GetMapping("list")
    public Resp getList() {
        return Resp.ok(permissionService.getList());
    }


    /**
     * 保存权限所关联的接口集合
     *
     * @return
     */
    @PostMapping("interface/mapping/save")
    public Resp savePermissionInterfaceMapping(@RequestBody @Validated PermissionInterfaceDTO permissionInterfaceDTO) {
        return Resp.ok(permissionInterfaceService.saveInterfaceMapping(permissionInterfaceDTO));
    }

    /**
     * 根据权限id获取接口列表
     *
     * @param permissionId
     * @return
     */
    @GetMapping("interface/list")
    public Resp getInterfaceListByPermissionId(@RequestParam("permissionId") Integer permissionId) {
        return Resp.ok(permissionInterfaceService.getInterfaceListByPermissionId(permissionId));
    }


    /**
     * 根据useId获取用户的权限list
     *
     * @param userId
     * @return
     */
    @GetMapping("list/by/user")
    public Resp getUserPermissionList(@RequestParam("userId") Integer userId) {
        return Resp.ok(sysUserPermissionService.getPermissionList(userId));
    }


    /**
     * 新增用户权限
     *
     * @param userPermissionDTO
     * @return
     */
    @PostMapping("user/mapping/add")
    public Resp addUserPermissionMapping(@RequestBody @Validated UserPermissionDTO userPermissionDTO,
                                         @RequestHeader(GlobalConst.USER_ID_HEADER) Integer createUserId) {
        return Resp.okOrFail(sysUserPermissionService.add(userPermissionDTO, createUserId));
    }


    /**
     * 删除用户权限
     *
     * @param userPermissionDTO
     * @return
     */
    @PostMapping("user/mapping/delete")
    public Resp deleteUserPermissionMapping(@RequestBody @Validated UserPermissionDTO userPermissionDTO) {
        return Resp.okOrFail(sysUserPermissionService.delete(userPermissionDTO));
    }


}
