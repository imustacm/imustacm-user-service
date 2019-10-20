package cn.imustacm.user.feign;

import cn.imustacm.common.domain.Resp;
import cn.imustacm.user.dto.UserBaseInfoDTO;
import cn.imustacm.user.model.Users;
import cn.imustacm.user.service.IUsersService;
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

    @Override
    public Users getUser(Long userId) {
        if (Objects.isNull(userId)) {
            return null;
        }
        return usersService.getById(userId);
    }
}