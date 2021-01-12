package cn.imustacm.user.service.impl;


import cn.imustacm.user.dto.PermissionDTO;
import cn.imustacm.user.mapper.SysPermissionMapper;
import cn.imustacm.user.model.SysPermission;
import cn.imustacm.user.service.SysPermissionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
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
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {


    @Override
    public List<PermissionDTO> getList() {
        LambdaQueryWrapper<SysPermission> wrapper = new QueryWrapper<SysPermission>().lambda()
                .eq(SysPermission::getVisible, true);
        List<SysPermission> list = list(wrapper);
        return buildDTOList(list);
    }

    @Override
    public List<PermissionDTO> batchGetList(List<Integer> permissionIdList) {
        if(CollectionUtils.isEmpty(permissionIdList)){
            return Lists.newArrayList();
        }
        LambdaQueryWrapper<SysPermission> wrapper = new QueryWrapper<SysPermission>().lambda()
                .in(SysPermission::getId, permissionIdList)
                .eq(SysPermission::getVisible, true);
        List<SysPermission> list = list(wrapper);
        return buildDTOList(list);
    }

    /**
     * 转DTO
     *
     * @param permissionList
     * @return
     */
    private List<PermissionDTO> buildDTOList(List<SysPermission> permissionList) {
        return permissionList.stream()
                .map(e -> PermissionDTO
                        .builder()
                        .id(e.getId())
                        .permissionName(e.getPermissionName())
                        .description(e.getDescription())
                        .build())
                .collect(Collectors.toList());
    }


}
