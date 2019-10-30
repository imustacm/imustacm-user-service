package cn.imustacm.user.feign;

import cn.imustacm.common.domain.Resp;
import cn.imustacm.user.dto.RankListDTO;
import cn.imustacm.user.dto.UserBaseInfoDTO;
import cn.imustacm.user.model.Option;
import cn.imustacm.user.model.Users;
import cn.imustacm.user.service.IUsersService;
import cn.imustacm.user.service.OptionService;
import cn.imustacm.user.service.UsersService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
public class UsersServiceFeign implements IUsersService {

    @Autowired
    private UsersService usersService;
    @Autowired
    private OptionService optionService;

    /**
     * 分页获取用户列表
     */
    @Override
    public Resp getUserList(Integer pageIndex, Integer pageSize) {
        if (Objects.isNull(pageIndex) || Objects.isNull(pageSize)) {
            return Resp.ok();
        }
        Page<Users> page = usersService.getList(pageIndex, pageSize);
        List<Users> userList = page.getRecords();
        if (CollectionUtils.isEmpty(userList)) {
            return Resp.ok(new Page<>());
        }
        // 实体类转换成dto
        List<UserBaseInfoDTO> userBaseInfoDTOList = userList.stream()
                .map(users -> UserBaseInfoDTO
                        .builder()
                        .name(users.getRealname())
                        .username(users.getUsername())
                        .school(users.getSchool())
                        .build())
                .collect(Collectors.toList());
        // 将dto集合封装到分页对象
        Page<UserBaseInfoDTO> result = new Page<>(page.getCurrent(), userBaseInfoDTOList.size(), page.getTotal());
        result.setRecords(userBaseInfoDTOList);
        return Resp.ok(result);
    }

    /**
     * 根据id获取用户信息
     */
    @Override
    public Users getUser(Long userId) {
        if (Objects.isNull(userId)) {
            return null;
        }
        return usersService.getById(userId);
    }

    /**
     * 根据key获取设置项
     */
    @Override
    public Option getByKey(String key) {
        return optionService.getByKey(key);
    }

    /**
     * 分页获取解题排名
     */
    @Override
    public Page<RankListDTO> getRankList(Integer pageIndex, Integer pageSize) {
        if (Objects.isNull(pageIndex) || Objects.isNull(pageSize)) {
            pageIndex = 1;
            pageSize = 100;
        }
        Page<Users> page = usersService.getRankList(pageIndex, pageSize);
        List<Users> userList = page.getRecords();
        if (CollectionUtils.isEmpty(userList)) {
            return new Page<>(pageIndex, 0, page.getTotal());
        }
        // 实体类转换成dto
        List<RankListDTO> rankListDTOs = userList.stream()
                .map(users -> RankListDTO
                        .builder()
                        .username(users.getUsername())
                        .realname(users.getRealname())
                        .signature(users.getSignature())
                        .gender(users.getGender())
                        .school(users.getSchool())
                        .major(users.getMajor())
                        .grade(users.getGrade())
                        .avatar(users.getAvatar())
                        .submit(users.getSubmit())
                        .solved(users.getSolved())
                        .build())
                .collect(Collectors.toList());
        // 将dto集合封装到分页对象
        Page<RankListDTO> result = new Page<>(page.getCurrent(), rankListDTOs.size(), page.getTotal());
        result.setRecords(rankListDTOs);
        return result;
    }
}