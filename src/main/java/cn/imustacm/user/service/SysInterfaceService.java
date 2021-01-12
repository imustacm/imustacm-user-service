package cn.imustacm.user.service;

import cn.imustacm.common.domain.PageParam;
import cn.imustacm.user.dto.InterfaceDTO;
import cn.imustacm.user.model.SysInterface;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liandong
 * @since 2020-12-27
 */
public interface SysInterfaceService extends IService<SysInterface> {


    /**
     * 分页查询接口信息
     *
     * @param pageParam
     * @return
     */
    Page<InterfaceDTO> getPage(PageParam pageParam);


    /**
     * 根据id批量获取接口列表
     *
     * @param interfaceIdList
     * @return
     */
    List<InterfaceDTO> getList(List<Integer> interfaceIdList);

}
