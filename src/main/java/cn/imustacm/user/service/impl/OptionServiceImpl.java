package cn.imustacm.user.service.impl;

import cn.imustacm.user.mapper.OptionMapper;
import cn.imustacm.user.model.Option;
import cn.imustacm.user.model.Users;
import cn.imustacm.user.service.OptionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 服务实现类
 *
 * @author wangjianli
 * @since 2019-10-24
 */
@Service
public class OptionServiceImpl extends ServiceImpl<OptionMapper, Option> implements OptionService {

    @Override
    public Option getByKey(String key) {
        LambdaQueryWrapper<Option> wrapper = new QueryWrapper<Option>().lambda().eq(Option::getKey, key);
        Option option = getOne(wrapper);
        return option;
    }

}
