package cn.imustacm.user.service.impl;

import cn.imustacm.user.dto.InterfaceDTO;
import cn.imustacm.user.dto.PermissionDTO;
import cn.imustacm.user.dto.PermissionInterfaceDTO;
import cn.imustacm.user.mapper.SysPermissionInterfaceMapper;
import cn.imustacm.user.model.SysPermissionInterface;
import cn.imustacm.user.service.SysInterfaceService;
import cn.imustacm.user.service.SysPermissionInterfaceService;
import cn.imustacm.user.service.SysPermissionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
public class SysPermissionInterfaceServiceImpl extends ServiceImpl<SysPermissionInterfaceMapper, SysPermissionInterface> implements SysPermissionInterfaceService {


    @Autowired
    private SysInterfaceService interfaceService;

    @Autowired
    private SysPermissionService permissionService;

    @Override
    //@Transactional(rollbackFor = Exception.class)
    public boolean saveInterfaceMapping(PermissionInterfaceDTO permissionInterfaceDTO) {
        if (Objects.isNull(permissionInterfaceDTO)) {
            return true;
        }
        List<Integer> interfaceIdList = permissionInterfaceDTO.getInterfaceIdList();
        if (CollectionUtils.isEmpty(interfaceIdList)) {
            return true;
        }
        List<InterfaceDTO> interfaceDTOList = interfaceService.getList(interfaceIdList);
        Map<Integer, String> interfaceId2UrlMap = interfaceDTOList.stream().collect(Collectors.toMap(InterfaceDTO::getId, InterfaceDTO::getInterfaceUrl));

        List<SysPermissionInterface> sysPermissionInterfaceList = interfaceIdList
                .stream()
                .map(e -> SysPermissionInterface
                        .builder()
                        .permissionId(permissionInterfaceDTO.getPermissionId())
                        .interfaceId(e)
                        .interfaceUrl(interfaceId2UrlMap.get(e))
                        .build())
                .collect(Collectors.toList());

        // 1 删除旧关联关系
        LambdaQueryWrapper<SysPermissionInterface> deleteWrapper = new QueryWrapper<SysPermissionInterface>().lambda()
                .eq(SysPermissionInterface::getPermissionId, permissionInterfaceDTO.getPermissionId());
        remove(deleteWrapper);
        // 2 保存新的关联关系
        return saveBatch(sysPermissionInterfaceList);
    }

    @Override
    public List<InterfaceDTO> getInterfaceListByPermissionId(Integer permissionId) {
        LambdaQueryWrapper<SysPermissionInterface> queryWrapper = new QueryWrapper<SysPermissionInterface>().lambda()
                .eq(SysPermissionInterface::getPermissionId, permissionId);
        List<SysPermissionInterface> list = list(queryWrapper);
        List<Integer> interfaceIdList = list.stream().map(SysPermissionInterface::getInterfaceId).collect(Collectors.toList());
        return interfaceService.getList(interfaceIdList);
    }

    @Override
    public Set<String> getInterfacePermissionNameSet(String url) {
        LambdaQueryWrapper<SysPermissionInterface> wrapper = new QueryWrapper<SysPermissionInterface>().lambda()
                .eq(SysPermissionInterface::getInterfaceUrl, url)
                .eq(SysPermissionInterface::getVisible, true);
        List<SysPermissionInterface> permissionInterfaceList = list(wrapper);
        List<Integer> permissionIdList = permissionInterfaceList.stream().map(SysPermissionInterface::getPermissionId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(permissionIdList)) {
            return Sets.newHashSet();
        }
        List<PermissionDTO> permissionDTOS = permissionService.batchGetList(permissionIdList);
        return permissionDTOS.stream().map(PermissionDTO::getPermissionName).collect(Collectors.toSet());
    }

}
