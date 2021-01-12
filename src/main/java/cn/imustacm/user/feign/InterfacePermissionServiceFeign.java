package cn.imustacm.user.feign;

import cn.imustacm.user.service.IInterfacePermissionService;
import cn.imustacm.user.service.SysPermissionInterfaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class InterfacePermissionServiceFeign implements IInterfacePermissionService {

    @Autowired
    private SysPermissionInterfaceService permissionInterfaceService;

    @Override
    public Set getInterfacePermissionSet(String servletPath) {
        return permissionInterfaceService.getInterfacePermissionNameSet(servletPath);
    }
}