package cn.imustacm.user.feign;

import cn.imustacm.user.IUsersService;
import cn.imustacm.user.model.Users;
import cn.imustacm.user.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class UsersFegin implements IUsersService {

    @Autowired
    private UsersService usersService;


    @Override
    public Boolean register() {

        // 0 创建一个用户对象
        Users user = Users.builder()
                .username("test")
                .gender(1)
                .build();
        return usersService.save(user);
    }
}
