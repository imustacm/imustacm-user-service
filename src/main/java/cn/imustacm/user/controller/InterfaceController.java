package cn.imustacm.user.controller;

import cn.imustacm.common.domain.PageParam;
import cn.imustacm.common.domain.Resp;
import cn.imustacm.user.dto.InterfaceDTO;
import cn.imustacm.user.model.SysInterface;
import cn.imustacm.user.service.SysInterfaceService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 接口管理controller
 */

@RestController
@RequestMapping("/interface")
@Slf4j
public class InterfaceController {

    @Autowired
    private SysInterfaceService sysInterfaceService;


    /**
     * 新增或修改接口
     *
     * @param interfaceDTO
     * @return
     */
    @PostMapping("save/or/update")
    public Resp saveOrUpdate(@RequestBody @Validated InterfaceDTO interfaceDTO) {
        SysInterface entity = SysInterface.builder()
                .id(interfaceDTO.getId())
                .serviceName(interfaceDTO.getServiceName())
                .interfaceUrl(interfaceDTO.getInterfaceUrl())
                .description(interfaceDTO.getDescription())
                .visible(interfaceDTO.getVisible())
                .build();
        boolean flag = sysInterfaceService.saveOrUpdate(entity);
        return Resp.okOrFail(flag);
    }


    /**
     * 根据接口id查询接口详情
     *
     * @param interfaceId
     * @return
     */
    @GetMapping("get/by/id")
    public Resp getInterfaceDetail(@RequestParam("interfaceId") Integer interfaceId) {
        SysInterface sysInterface = sysInterfaceService.getById(interfaceId);
        if (Objects.isNull(sysInterface)) {
            return Resp.fail();
        }
        InterfaceDTO dto = InterfaceDTO.builder()
                .id(sysInterface.getId())
                .serviceName(sysInterface.getServiceName())
                .interfaceUrl(sysInterface.getInterfaceUrl())
                .description(sysInterface.getDescription())
                .visible(sysInterface.getVisible())
                .build();
        return Resp.ok(dto);
    }

    /**
     * 分页查询接口
     *
     * @param pageParam
     * @return
     */
    @PostMapping("page")
    public Resp getPage(@RequestBody PageParam pageParam) {
        Page<InterfaceDTO> page = sysInterfaceService.getPage(pageParam);
        return Resp.ok(page);
    }

}
