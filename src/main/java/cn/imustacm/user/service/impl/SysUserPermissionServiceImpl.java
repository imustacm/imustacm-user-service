package cn.imustacm.user.service.impl;


import cn.imustacm.user.dto.PermissionDTO;
import cn.imustacm.user.dto.UserPermissionDTO;
import cn.imustacm.user.mapper.SysUserPermissionMapper;
import cn.imustacm.user.model.SysUserPermission;
import cn.imustacm.user.service.SysPermissionService;
import cn.imustacm.user.service.SysUserPermissionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author liandong
 * @since 2020-12-27
 */
@Service
public class SysUserPermissionServiceImpl extends ServiceImpl<SysUserPermissionMapper, SysUserPermission> implements SysUserPermissionService {


    @Autowired
    private SysPermissionService sysPermissionService;

    @Override
    public List<PermissionDTO> getPermissionList(Integer userId) {
        LambdaQueryWrapper<SysUserPermission> queryWrapper = new QueryWrapper<SysUserPermission>().lambda()
                .eq(SysUserPermission::getUserId, userId)
                .eq(SysUserPermission::getVisible, true);
        List<SysUserPermission> list = list(queryWrapper);
        List<Integer> permissionIdList = list.stream().map(SysUserPermission::getPermissionId).collect(Collectors.toList());
        return sysPermissionService.batchGetList(permissionIdList);
    }

    @Override
    public boolean delete(UserPermissionDTO userPermissionDTO) {

        SysUserPermission userPermission = getUserPermission(userPermissionDTO.getUserId(), userPermissionDTO.getPermissionId());
        if (Objects.isNull(userPermission)) {
            return true;
        }
        if (!userPermission.getVisible()) {
            return true;
        }
        SysUserPermission updateEntity = SysUserPermission
                .builder()
                .id(userPermission.getId())
                .visible(false)
                .build();
        return updateById(updateEntity);
    }

    @Override
    public boolean add(UserPermissionDTO userPermissionDTO, Integer createUserId) {
        SysUserPermission userPermission = getUserPermission(userPermissionDTO.getUserId(), userPermissionDTO.getPermissionId());
        if (Objects.isNull(userPermission)) {
            SysUserPermission entity = SysUserPermission
                    .builder()
                    .createUser(createUserId)
                    .userId(userPermissionDTO.getUserId())
                    .permissionId(userPermissionDTO.getPermissionId())
                    .visible(true)
                    .build();
            return save(entity);
        }
        if (!userPermission.getVisible()) {
            SysUserPermission updateEntity = SysUserPermission
                    .builder()
                    .id(userPermission.getId())
                    .createUser(createUserId)
                    .visible(true)
                    .build();
            return updateById(updateEntity);
        }
        return true;
    }

    private SysUserPermission getUserPermission(Integer userId, Integer permissionId) {
        LambdaQueryWrapper<SysUserPermission> queryWrapper = new QueryWrapper<SysUserPermission>().lambda()
                .eq(SysUserPermission::getUserId, userId)
                .eq(SysUserPermission::getPermissionId, permissionId);
        return getOne(queryWrapper);
    }
}
