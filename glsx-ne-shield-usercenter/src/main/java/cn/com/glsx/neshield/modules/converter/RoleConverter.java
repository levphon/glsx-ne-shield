package cn.com.glsx.neshield.modules.converter;

import cn.com.glsx.neshield.modules.model.view.RoleDTO;
import cn.com.glsx.neshield.modules.entity.Role;
import cn.com.glsx.neshield.modules.model.param.RoleBO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RoleConverter {

    RoleConverter INSTANCE = Mappers.getMapper(RoleConverter.class);

    //RoleDTO do2dto(Role role);

    @Mappings({
            @Mapping(source = "roleId", target = "id"),
            @Mapping(source = "roleName", target = "roleName"),
            @Mapping(source = "remark", target = "remark"),
            @Mapping(source = "permissionType", target = "rolePermissionType"),
            @Mapping(source = "visibleType", target = "roleVisibility"),
//            @Mapping(source = "visibleTenant", target = "roleTenants"),
            @Mapping(source = "enableStatus", target = "enableStatus"),
    })
    Role boToDo(RoleBO roleBO);

}
