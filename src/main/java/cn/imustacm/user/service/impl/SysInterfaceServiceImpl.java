package cn.imustacm.user.service.impl;


import cn.imustacm.common.domain.PageParam;
import cn.imustacm.user.dto.InterfaceDTO;
import cn.imustacm.user.mapper.SysInterfaceMapper;
import cn.imustacm.user.model.SysInterface;
import cn.imustacm.user.service.SysInterfaceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class SysInterfaceServiceImpl extends ServiceImpl<SysInterfaceMapper, SysInterface> implements SysInterfaceService {

    @Override
    public Page<InterfaceDTO> getPage(PageParam pageParam) {
        LambdaQueryWrapper<SysInterface> queryWrapper = new QueryWrapper<SysInterface>().lambda().orderByAsc(SysInterface::getId);
        Page<SysInterface> page = (Page<SysInterface>) page(new Page<>(pageParam.getPageIndex(), pageParam.getPageSize()), queryWrapper);
        List<SysInterface> records = page.getRecords();
        // 转化DTO对象 重新封装为Page对象
        List<InterfaceDTO> dtoList = buildDTOList(records);
        Page<InterfaceDTO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(dtoList);
        return result;
    }

    @Override
    public List<InterfaceDTO> getList(List<Integer> interfaceIdList) {
        LambdaQueryWrapper<SysInterface> queryWrapper = new QueryWrapper<SysInterface>().lambda()
                .in(SysInterface::getId, interfaceIdList);
        List<SysInterface> list = list(queryWrapper);

        return buildDTOList(list);
    }

    /**
     * 转DTO
     *
     * @param interfaceList
     * @return
     */
    private List<InterfaceDTO> buildDTOList(List<SysInterface> interfaceList) {
        return interfaceList
                .stream()
                .map(e -> InterfaceDTO
                        .builder()
                        .id(e.getId())
                        .serviceName(e.getServiceName())
                        .interfaceUrl(e.getInterfaceUrl())
                        .description(e.getDescription())
                        .visible(e.getVisible())
                        .build())
                .collect(Collectors.toList());
    }
}
