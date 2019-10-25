package cn.imustacm.user.service;

import cn.imustacm.user.model.Option;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 服务类
 *
 * @author wangjianli
 * @since 2019-10-24
 */
public interface OptionService extends IService<Option> {

    /**
     * 根据key查询配置信息
     *
     * @param key
     * @return
     */
    Option getByKey(String key);

}
